package server;

import javax.websocket.Session;
import java.util.*;

public class ConnectionManager {
    private static final Map<Integer, Set<Session>> gameConnections = new HashMap<>();

    public static void addConnection(int gameID, Session session) {
        // implement
    }

    public static void removeConnection(int gameID, Session session) {
        //implement
    }

    public static void broadcastToGame(int gameID, Object serverMessage) {
        //implement
    }
}
