package client;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class ServerAccess {

    private final String baseUrl;
    private String authToken;

    public ServerAccess(String baseUrl) {
        this.baseUrl = baseUrl;
        this.authToken = null;
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

    private Map<String,Object> doCall(String method, String endpoint, Object body) {
        Map<String,Object> out = new HashMap<>();
        try {
            URI uri = new URI(baseUrl + endpoint);
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod(method);

            if (authToken != null) {
                http.setRequestProperty("authorization", authToken);
            }

            if (body != null) {
                http.setDoOutput(true);
                http.setRequestProperty("Content-Type","application/json");
            }

            http.connect();

            if (body != null) {
                String json = new Gson().toJson(body);
                try(OutputStream os = http.getOutputStream()) {
                    os.write(json.getBytes());
                }
            }

            int code = http.getResponseCode();
            if (code >= 400) {
                try (InputStream err = http.getErrorStream()) {
                    if (err != null) {
                        var parsed = new Gson().fromJson(new InputStreamReader(err), Map.class);
                        if (parsed == null) parsed = new HashMap<>();
                        out.putAll(parsed);
                    }
                }
                out.put("Error","HTTP " + code);
                return out;
            }
            try (InputStream in = http.getInputStream();
                 InputStreamReader rdr = new InputStreamReader(in)) {
                Map<String,Object> parsed = new Gson().fromJson(rdr, Map.class);
                if (parsed == null) parsed = new HashMap<>();
                return parsed;
            }
        } catch (URISyntaxException | IOException e) {
            out.put("Error", e.getMessage());
            return out;
        }
    }
}
