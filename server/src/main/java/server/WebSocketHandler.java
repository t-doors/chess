package server;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint("/ws")
public class WebSocketHandler {

    private static final Map<Session, Integer> sessionGameMap = new HashMap<>();

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
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error in session " + (session != null ? session.getId() : "null"));
        throwable.printStackTrace();
    }
}
