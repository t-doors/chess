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

import java.util.HashMap;
import java.util.Map;

@WebSocket
public class WebSocketHandler {

    private static AuthDAO authDAO;
    private static GameDAO gameDAO;
    private static UserDAO userDAO;

    private static final Map<Session, Integer> sessionGameMap = new HashMap<>();

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
        Integer gameID = sessionGameMap.get(session);
        if (gameID != null) {
            ConnectionManager.removeConnection(gameID, session);
            sessionGameMap.remove(session);
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
            case RESIGN -> handleResign(cmd, session);
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error in session "
                + (session != null ? session.getRemoteAddress() : "null"));
        throwable.printStackTrace();
    }

    // ===== CONNECT =====
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

            sessionGameMap.put(session, gameID);
            ConnectionManager.addConnection(gameID, session);

            sendLoadGame(session, gameData);

            String role = determineRole(gameData, authData.username());
            broadcastNotification(gameID, authData.username() + " connected as " + role);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    // ===== MAKE_MOVE =====
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
            var chessGame = gameData.game();

            if (!isUsersTurn(chessGame, gameData, username)) {
                sendError(session, "Error: Not your turn or you're observer");
                return;
            }

            if (cmd.getMove() == null) {
                sendError(session, "Error: Missing move data");
                return;
            }
            boolean moveOk = applyMove(chessGame, cmd.getMove());
            if (!moveOk) {
                sendError(session, "Error: Illegal move");
                return;
            }

            GameData updated = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    gameData.blackUsername(),
                    gameData.gameName(),
                    chessGame
            );

            gameDAO.updateGame(updated);
            broadcastLoadGame(updated);

            String moveDesc = describeMove(cmd.getMove());
            broadcastNotification(gameID, username + " moved " + moveDesc);
            checkEndgameConditions(chessGame, username, gameID);

        } catch (DataAccessException ex) {
            sendError(session, "Error: " + ex.getMessage());
        }
    }

    // ===== RESIGN =====
    private void handleResign(UserGameCommand cmd, Session session) {
        String token = cmd.getAuthToken();
        Integer gameID = cmd.getGameID();

        if (token == null || gameID == null) {
            sendError(session, "Error: Missing token or gameID for RESIGN");
            return;
        }

        try {
            AuthData authData = authDAO.getAuth(token); // throws if not found
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

            broadcastNotification(gameID, username + " resigned the game. Game over.");

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private boolean isUsersTurn(Object chessGame, GameData data, String username) {
        return true;
    }

    private boolean applyMove(Object chessGame, Object move) {
        return true;
    }

    private void checkEndgameConditions(Object chessGame, String username, Integer gameID) {
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

    private void broadcastNotification(Integer gameID, String message) {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
        msg.setMessage(message);
        String json = new Gson().toJson(msg);
        ConnectionManager.broadcastToGame(gameID, json);
    }

    private String determineRole(GameData gameData, String username) {
        if (username.equals(gameData.whiteUsername())) return "WHITE";
        if (username.equals(gameData.blackUsername())) return "BLACK";
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
