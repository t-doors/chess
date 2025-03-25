package client;

import java.util.*;


public class ServerAccess {

    private final String baseUrl;
    private String authToken;

    public ServerAccess(String baseUrl) {
        this.baseUrl = baseUrl;
        this.authToken = null;
    }

    public Map<String,Object> doCall(String method, String endpoint, Object body) {
        return new HashMap<>();
    }

    public boolean register(String username, String password, String email) {
        Map<String,Object> resp = doCall("POST", "/user", Map.of(
                "username", username,
                "password", password,
                "email", email
        ));
        if (resp.containsKey("authToken")) {
            authToken = (String) resp.get("authToken");
            return true;
        }
        return false;
    }

    public boolean login(String username, String password) {
        Map<String, Object> payload = Map.of(
                "username", username,
                "password", password
        );

        Map<String, Object> resp = doCall("POST", "/session", payload);
        if (resp.containsKey("authToken")) {
            this.authToken = (String) resp.get("authToken");
            return true;
        }
        return false;
    }

    public boolean logout() {
        Map<String, Object> resp = doCall("DELETE", "/session", null);
        if (resp.containsKey("Error")) {
            return false;
        }
        this.authToken = null;
        return true;
    }

    public int createGame(String gameName) {
        // TODO: implement
        return -1;
    }

    public List<Map<String,Object>> listGames() {
        // TODO: implement
        return List.of();
    }

    public boolean joinGame(int gameID, String color) {
        // TODO: implement
        return false;
    }
}
