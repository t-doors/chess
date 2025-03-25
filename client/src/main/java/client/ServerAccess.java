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
        Map<String,Object> resp = doCall("POST", "/game", Map.of("gameName", gameName));
        if (resp.containsKey("gameID")) {
            double d = (double) resp.get("gameID");
            return (int)d;
        }
        return -1;
    }

    public List<Map<String,Object>> listGames() {
        Map<String,Object> resp = doCall("GET", "/game", null);
        if (resp.containsKey("games")) {
            Object gObj = resp.get("games");
            if (gObj instanceof List<?> list) {
                List<Map<String,Object>> res = new ArrayList<>();
                for (Object itm : list) {
                    if (itm instanceof Map<?,?> mp) {
                        var casted = new HashMap<String,Object>();
                        mp.forEach((k,v)-> casted.put(k.toString(), v));
                        res.add(casted);
                    }
                }
                return res;
            }
        }
        return List.of();
    }

    public boolean joinGame(int gameID, String color) {
        Map<String,Object> payload = new HashMap<>();
        payload.put("gameID", gameID);
        if (color != null) {
            payload.put("playerColor", color.toUpperCase());
        }
        Map<String,Object> resp = doCall("PUT", "/game", payload);
        return !resp.containsKey("Error") && !resp.containsKey("message");
    }
}
