package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/ws")
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

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket open: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("WebSocket close: " + session.getId());
        Integer gameID = sessionGameMap.get(session);
        if (gameID != null) {
            ConnectionManager.removeConnection(gameID, session);
            sessionGameMap.remove(session);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received raw message: " + message);
        Gson gson = new Gson();
        UserGameCommand cmd = gson.fromJson(message, UserGameCommand.class);
        if (cmd == null) {
            sendError(session, "Error: Invalid JSON in UserGameCommand");
            return;
        }
        switch (cmd.getCommandType()) {
            case CONNECT -> handleConnect(cmd, session);
        }
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

            sessionGameMap.put(session, gameID);
            ConnectionManager.addConnection(gameID, session);

            sendLoadGame(session, gameData);

            String role = determineRole(gameData, authData.username());
            broadcastNotification(gameID, authData.username() + " connected as " + role);

        } catch (DataAccessException e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private void sendLoadGame(Session session, GameData gameData) {
        ServerMessage msg = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME);
        msg.setGame(gameData);
        String json = new Gson().toJson(msg);
        sendToSession(session, json);
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
        System.out.println("Sending error to " + session.getId() + ": " + msg);
        ServerMessage sm = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        sm.setErrorMessage(msg);
        String json = new Gson().toJson(sm);
        sendToSession(session, json);
    }

    private void sendToSession(Session session, String json) {
        try {
            session.getBasicRemote().sendText(json);
        } catch (Exception e) {
            System.err.println("Failed to send to session " + session.getId() + ": " + e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error in session " + (session != null ? session.getId() : "null"));
        throwable.printStackTrace();
    }
}
