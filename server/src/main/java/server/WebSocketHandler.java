package server;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws")
public class WebSocketHandler {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket open: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("WebSocket close: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received raw message: " + message);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error in session " + session.getId());
        throwable.printStackTrace();
    }
}
