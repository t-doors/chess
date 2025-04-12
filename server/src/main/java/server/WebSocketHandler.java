package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import chess.*;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@WebSocket
public class WebSocketHandler {

    private static AuthDAO authDAO;
    private static GameDAO gameDAO;
    private static UserDAO userDAO;

    private static final Map<Session, Integer> SESSION_GAME_MAP = new HashMap<>();

    public static void initialize(AuthDAO aDao, GameDAO gDao, UserDAO uDao) {
        authDAO = aDao;
        gameDAO = gDao;
        userDAO = uDao;
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
        System.out.println("WebSocket open: " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket close: " + session.getRemoteAddress());
        Integer gameID = SESSION_GAME_MAP.get(session);
        if (gameID != null) {
            ConnectionManager.removeConnection(gameID, session);
            SESSION_GAME_MAP.remove(session);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        System.out.println("Received raw message: " + message);
        Gson gson = new Gson();
        UserGameCommand cmd = gson.fromJson(message, UserGameCommand.class);
        if (cmd == null) {
            sendError(session, "Error: Invalid JSON in UserGameCommand");
            return;
        }

        switch (cmd.getCommandType()) {
            case CONNECT -> handleConnect(cmd, session);
            case MAKE_MOVE -> handleMakeMove(cmd, session);
            case LEAVE -> handleLeave(cmd, session);
            case RESIGN -> handleResign(cmd, session);
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error in session "
                + (session != null ? session.getRemoteAddress() : "null"));
        throwable.printStackTrace();
    }

    private void handleConnect(UserGameCommand cmd, Session session) {
        String token = cmd.getAuthToken();
        Integer gameID = cmd.getGameID();

        try {
            AuthData authData = authDAO.getAuth(token);
            if (authData == null) {
                sendError(session, "Error: Invalid token (null AuthData)");
                return;
            }
            GameData gameData = gameDAO.getGame(gameID);

            SESSION_GAME_MAP.put(session, gameID);
            ConnectionManager.addConnection(gameID, session);

            sendLoadGame(session, gameData);

            String role = determineRole(gameData, authData.username());
            broadcastNotification(gameID, session, authData.username() + " connected as " + role);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }


    private void handleMakeMove(UserGameCommand cmd, Session session) {
        String token = cmd.getAuthToken();
        if (token == null) {
            sendError(session, "Error: Missing auth token");
            return;
        }

        try {
            AuthData authData = authDAO.getAuth(token);
            if (authData == null) {
                sendError(session, "Error: Invalid token (no auth data)");
                return;
            }

            String username = authData.username();

            Integer gameID = cmd.getGameID();
            if (gameID == null) {
                sendError(session, "Error: Missing gameID");
                return;
            }
            GameData gameData = gameDAO.getGame(gameID);

            ChessGame chessGame = gameData.game();
            if (chessGame == null) {
                chessGame = new ChessGame();
                gameData = new GameData(
                        gameData.gameID(),
                        gameData.whiteUsername(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        chessGame
                );
                gameDAO.updateGame(gameData);
            }

            if (isGameOver(gameData)) {
                sendError(session, "Error: The game is over.");
                return;
            }

            boolean isWhite = username.equals(gameData.whiteUsername());
            boolean isBlack = username.equals(gameData.blackUsername());
            if (!isWhite && !isBlack) {
                sendError(session, "Error: Observers cannot make moves.");
                return;
            }

            if (chessGame.getTeamTurn() == ChessGame.TeamColor.WHITE && !isWhite) {
                sendError(session, "Error: Wrong turn (white's turn).");
                return;
            }
            if (chessGame.getTeamTurn() == ChessGame.TeamColor.BLACK && !isBlack) {
                sendError(session, "Error: Wrong turn (black's turn).");
                return;
            }

            if (cmd.getMove() == null) {
                sendError(session, "Error: Missing move data");
                return;
            }

            try {
                chessGame.makeMove(cmd.getMove());
            } catch (InvalidMoveException e) {
                sendError(session, "Error: " + e.getMessage());
                return;
            }

            if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) ||
                    chessGame.isInCheckmate(ChessGame.TeamColor.BLACK) ||
                    chessGame.isInStalemate(ChessGame.TeamColor.WHITE) ||
                    chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
                gameData = new GameData(gameData.gameID(), null, null, gameData.gameName(), chessGame);
                gameDAO.updateGame(gameData);
            } else {
                gameDAO.updateGame(gameData);
            }

            broadcastLoadGame(gameData);

            String moveDesc = describeMove(cmd.getMove());
            broadcastNotification(gameID, session, username + " moved " + moveDesc);

            checkEndgameConditions(chessGame, username, gameID);

        } catch (DataAccessException ex) {
            sendError(session, "Error: " + ex.getMessage());
        }
    }

    private void handleLeave(UserGameCommand cmd, Session session) {
        String token = cmd.getAuthToken();
        Integer gameID = cmd.getGameID();
        if (token == null || gameID == null) {
            sendError(session, "Error: Missing token or gameID for LEAVE");
            return;
        }
        try {
            AuthData authData = authDAO.getAuth(token);
            if (authData == null) {
                sendError(session, "Error: Invalid auth token for LEAVE");
                return;
            }
            String username = authData.username();
            GameData gameData = gameDAO.getGame(gameID);

            if (username.equals(gameData.whiteUsername())) {
                gameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
            } else if (username.equals(gameData.blackUsername())) {
                gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
            }
            gameDAO.updateGame(gameData);

            ConnectionManager.removeConnection(gameID, session);
            SESSION_GAME_MAP.remove(session);
            broadcastNotification(gameID, session, username + " left the game.");
        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }


    private void handleResign(UserGameCommand cmd, Session session) {
        String token = cmd.getAuthToken();
        Integer gameID = cmd.getGameID();

        if (token == null || gameID == null) {
            sendError(session, "Error: Missing token or gameID for RESIGN");
            return;
        }

        try {
            AuthData authData = authDAO.getAuth(token);
            if (authData == null) {
                sendError(session, "Error: Invalid auth token for RESIGN");
                return;
            }
            String username = authData.username();
            GameData gameData = gameDAO.getGame(gameID);

            boolean isWhite = username.equals(gameData.whiteUsername());
            boolean isBlack = username.equals(gameData.blackUsername());
            if (!isWhite && !isBlack) {
                sendError(session, "Error: Observers cannot resign");
                return;
            }

            ChessGame chessGame = gameData.game();

            GameData updated = new GameData(
                    gameData.gameID(),
                    null,null,
                    gameData.gameName(),
                    chessGame
            );
            gameDAO.updateGame(updated);

            broadcastNotificationToAll(gameID, username + " resigned the game. Game over.");

            ConnectionManager.removeConnection(gameID, session);
            SESSION_GAME_MAP.remove(session);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private boolean isGameOver(GameData gameData) {
        return gameData.whiteUsername() == null && gameData.blackUsername() == null;
    }

    private void checkEndgameConditions(ChessGame chessGame, String username, Integer gameID) {
        if (chessGame.isInCheckmate(ChessGame.TeamColor.WHITE) || chessGame.isInCheckmate(ChessGame.TeamColor.BLACK)) {
            broadcastNotification(gameID, null, "Checkmate! Game over.");
        } else if (chessGame.isInStalemate(ChessGame.TeamColor.WHITE) || chessGame.isInStalemate(ChessGame.TeamColor.BLACK)) {
            broadcastNotification(gameID, null, "Stalemate! Game over.");
        }
    }

    private String describeMove(Object move) {
        return "(a move)";
    }

    private void sendLoadGame(Session session, GameData gameData) {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        msg.setGame(gameData);
        String json = new Gson().toJson(msg);
        sendToSession(session, json);
    }

    private void broadcastLoadGame(GameData gameData) {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        msg.setGame(gameData);
        String json = new Gson().toJson(msg);
        ConnectionManager.broadcastToGame(gameData.gameID(), json);
    }

    private void broadcastNotificationToAll(Integer gameID, String message) {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        msg.setMessage(message);
        String json = new Gson().toJson(msg);
        ConnectionManager.broadcastToGame(gameID, json);
    }



    private void broadcastNotification(Integer gameID, Session exclude, String message) {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        msg.setMessage(message);
        String json = new Gson().toJson(msg);

        Set<Session> sessions = ConnectionManager.getConnectionsForGame(gameID);
        for (Session s : sessions) {
            if ((exclude == null || !s.equals(exclude)) && s.isOpen()) {
                try {
                    s.getRemote().sendString(json);
                } catch (IOException e) {
                    System.err.println("Failed to send notification to "
                            + s.getRemoteAddress() + ": " + e.getMessage());
                }
            }
        }
    }




    private String determineRole(GameData gameData, String username) {
        if (username.equals(gameData.whiteUsername())){
            return "WHITE";
        }

        if (username.equals(gameData.blackUsername())){
            return "BLACK";
        }
        return "Observer";
    }

    private void sendError(Session session, String msg) {
        System.out.println("Sending error to " + session.getRemoteAddress() + ": " + msg);
        ServerMessage sm = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        sm.setErrorMessage(msg);
        String json = new Gson().toJson(sm);
        sendToSession(session, json);
    }

    private void sendToSession(Session session, String json) {
        try {
            session.getRemote().sendString(json);
        } catch (Exception e) {
            System.err.println("Failed to send to session " + session.getRemoteAddress() + ": " + e.getMessage());
        }
    }
}
