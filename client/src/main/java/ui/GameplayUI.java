package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import client.Board;
import client.ServerAccess;
import com.google.gson.Gson;
import model.GameData;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import javax.websocket.ContainerProvider;
import javax.websocket.ClientEndpoint;
import java.net.URI;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class GameplayUI {

    private final ServerAccess server;
    private Session wsSession;
    private final Board board;
    private final Scanner scanner;
    private final Gson gson = new Gson();

    private String authToken;
    private int gameID;
    private String role;
    private boolean blackView;

    private ChessGame currentGame;

    public GameplayUI(ServerAccess server) {
        this.server = server;
        this.board = new Board();
        this.scanner = new Scanner(System.in);
    }

    public void run(String authToken, int gameID, String role) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.role = role.toUpperCase();
        blackView = role.equalsIgnoreCase("BLACK");

        connectWebSocket();

        printHelp();

        while (true) {
            System.out.print("[gameplay] >>> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            String[] tokens = input.split("\\s+");
            String command = tokens[0].toLowerCase();
            switch (command) {
                case "help":
                    printHelp();
                    break;
                case "redraw":
                    board.clearHighlight();
                    board.drawChessBoard(currentGame, blackView);
                    break;
                case "move":
                    if (tokens.length != 2) {
                        System.out.println("Usage: e.g. move e2e4");
                    } else {
                        parseAndSendMove(tokens[1]);
                    }
                    break;
                case "resign":
                    System.out.print("Are you sure you want to resign? (yes/no): ");
                    String ans = scanner.nextLine().trim().toLowerCase();
                    if ("yes".equals(ans)) {
                        sendWebSocketCommand(UserGameCommand.CommandType.RESIGN, null);
                        System.out.println("You have resigned. Game over.");
                        disconnectWebSocket();
                        return;
                    }
                    break;
                case "leave":
                    sendWebSocketCommand(UserGameCommand.CommandType.LEAVE, null);
                    System.out.println("You have left the game.");
                    disconnectWebSocket();
                    return;
                case "highlight":
                    if (tokens.length != 2) {
                        System.out.println("Usage: e.g. highlight e2");
                    } else {
                        highlightSquare(tokens[1]);
                    }
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' for list of commands.");
            }
        }
    }

    private void printHelp() {
        System.out.println("Gameplay commands:");
        System.out.println("  help                    : Show this help message.");
        System.out.println("  redraw                  : Redraw the chess board (clear highlights).");
        System.out.println("  move (eg. a2a4)         : Make a move (enter as 4 letter string).");
        System.out.println("  resign                  : Resign from the game.");
        System.out.println("  leave                   : Leave the game and return to postlogin UI.");
        System.out.println("  highlight (eg. a2)      : Highlight legal moves for the piece (enter at 2 letter string).");
    }

    private void parseAndSendMove(String moveStr) {
        if (moveStr.length() != 4) {
            System.out.println("Error: Move notation must be 4 characters, e.g. e2e4.");
            return;
        }
        String startSq = moveStr.substring(0, 2).toLowerCase();
        String endSq = moveStr.substring(2, 4).toLowerCase();
        try {
            ChessPosition startPos = parseAlgebraic(startSq);
            ChessPosition endPos = parseAlgebraic(endSq);
            ChessMove move = new ChessMove(startPos, endPos, null);
            sendWebSocketCommand(UserGameCommand.CommandType.MAKE_MOVE, move);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void highlightSquare(String sq) {
        if (currentGame == null) {
            System.out.println("Current game state not loaded yet.");
            return;
        }
        try {
            ChessPosition origin = parseAlgebraic(sq);
            Collection<ChessMove> legalMoves = currentGame.validMoves(origin);
            if (legalMoves == null || legalMoves.isEmpty()) {
                System.out.println("No legal moves for piece at " + sq);
                board.clearHighlight();
            } else {
                Set<ChessPosition> movesSet = legalMoves.stream()
                        .map(ChessMove::getEndPosition)
                        .collect(Collectors.toSet());
                board.highlightSquares(origin, movesSet);
            }
            board.drawChessBoard(currentGame, blackView);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private ChessPosition parseAlgebraic(String sq) {
        if (sq.length() != 2) {
            throw new IllegalArgumentException("Square must be 2 characters, e.g. e2.");
        }
        char file = sq.charAt(0);
        char rank = sq.charAt(1);
        if (file < 'a' || file > 'h') {
            throw new IllegalArgumentException("File must be between a and h.");
        }
        if (rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Rank must be between 1 and 8.");
        }
        int col = file - 'a' + 1;
        int row = rank - '0';
        return new ChessPosition(row, col);
    }

    private void sendWebSocketCommand(UserGameCommand.CommandType commandType, ChessMove move) {
        UserGameCommand command = new UserGameCommand(commandType, authToken, gameID, move);
        String json = gson.toJson(command);
        try {
            if (wsSession != null && wsSession.isOpen()) {
                wsSession.getAsyncRemote().sendText(json);
            } else {
                System.out.println("WebSocket is not connected.");
            }
        } catch (Exception e) {
            System.err.println("Error sending WebSocket command: " + e.getMessage());
        }
    }

    private void connectWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            int port = server.getPort();
            URI uri = new URI("ws://localhost:" + port + "/ws");
            wsSession = container.connectToServer(new GameplayEndpoint(), uri);
            sendWebSocketCommand(UserGameCommand.CommandType.CONNECT, null);
        } catch (Exception e) {
            System.err.println("WebSocket connection failed: " + e.getMessage());
        }
    }

    private void disconnectWebSocket() {
        try {
            if (wsSession != null && wsSession.isOpen()) {
                wsSession.close();
            }
        } catch (Exception e) {
            System.err.println("Error disconnecting WebSocket: " + e.getMessage());
        }
    }

    @ClientEndpoint
    public class GameplayEndpoint {

        private final Gson gson = new Gson();

        @OnOpen
        public void onOpen(Session session) {
            System.out.println("Gameplay WebSocket connected.");
        }

        @OnMessage
        public void onMessage(String message) {
            ServerMessage serverMsg = gson.fromJson(message, ServerMessage.class);
            if (serverMsg == null) return;

            switch (serverMsg.getServerMessageType()) {
                case LOAD_GAME -> {
                    if (serverMsg.getGame() != null) {
                        String gameJson = gson.toJson(serverMsg.getGame());
                        GameData gameData = gson.fromJson(gameJson, GameData.class);
                        currentGame = gameData.game();
                    }
                    System.out.println("Game state updated.");
                    board.drawChessBoard(currentGame, blackView);
                }
                case NOTIFICATION ->
                        System.out.println("Notification: " + serverMsg.getMessage());
                case ERROR ->
                        System.out.println("Error from server: " + serverMsg.getErrorMessage());
                default ->
                        System.out.println("Unknown message type received.");
            }
        }


        @OnError
        public void onError(Session session, Throwable t) {
            System.err.println("Gameplay WebSocket error: " + t.getMessage());
        }
    }
}
