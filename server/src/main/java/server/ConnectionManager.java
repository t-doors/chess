package server;

import org.eclipse.jetty.websocket.api.Session;
import java.io.IOException;
import java.util.*;

public class ConnectionManager {
    private static final Map<Integer, Set<Session>> GAME_CONNECTIONS = new HashMap<>();

    public static synchronized void addConnection(int gameID, Session session) {
        GAME_CONNECTIONS.putIfAbsent(gameID, new HashSet<>());
        GAME_CONNECTIONS.get(gameID).add(session);
    }

    public static synchronized void removeConnection(int gameID, Session session) {
        Set<Session> sessions = GAME_CONNECTIONS.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                GAME_CONNECTIONS.remove(gameID);
            }
        }
    }
    public static synchronized Set<Session> getConnectionsForGame(int gameID) {
        return GAME_CONNECTIONS.getOrDefault(gameID, Collections.emptySet());
    }

    public static synchronized void broadcastToGame(int gameID, String jsonMessage) {
        Set<Session> sessions = GAME_CONNECTIONS.get(gameID);
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
