package server;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.*;

public class ConnectionManager {
    private static final Map<Integer, Set<Session>> gameConnections = new HashMap<>();

    public static synchronized void addConnection(int gameID, Session session) {
        gameConnections.putIfAbsent(gameID, new HashSet<>());
        gameConnections.get(gameID).add(session);
    }

    public static synchronized void removeConnection(int gameID, Session session) {
        Set<Session> sessions = gameConnections.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                gameConnections.remove(gameID);
            }
        }
    }

    public static synchronized void broadcastToGame(int gameID, String jsonMessage) {
        Set<Session> sessions = gameConnections.get(gameID);
        if (sessions == null) {
            return;
        }

        for (Session s : sessions) {
            if (s.isOpen()) {
                try {
                    s.getRemote().sendString(jsonMessage);
                } catch (IOException e) {
                    System.err.println("Failed to send WebSocket message to session "
                            + s.getRemoteAddress() + ": " + e.getMessage());
                }
            }
        }
    }
}
