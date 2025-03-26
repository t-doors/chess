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


    public boolean register(String user, String pass, String email) {
        var resp = doCall("POST","/user", Map.of(
                "username",user, "password",pass, "email",email
        ));
        if (resp.containsKey("authToken")) {
            authToken = (String) resp.get("authToken");
            return true;
        }
        return false;
    }

    public boolean login(String user, String pass) {
        var resp = doCall("POST","/session", Map.of(
                "username", user, "password", pass
        ));
        if (resp.containsKey("authToken")) {
            authToken = (String) resp.get("authToken");
            return true;
        }
        return false;
    }

    public boolean logout() {
        var resp = doCall("DELETE","/session", null);
        if (resp.containsKey("Error")) {
            return false;
        }
        authToken = null;
        return !resp.containsKey("message");
    }

    public int createGame(String gameName) {
        var resp = doCall("POST","/game", Map.of("gameName", gameName));
        if (resp.containsKey("gameID")) {
            double d = (double) resp.get("gameID");
            return (int)d;
        }
        return -1;
    }

    public List<Map<String,Object>> listGames() {
        var resp = doCall("GET","/game",null);
        if (resp.containsKey("games")) {
            Object raw = resp.get("games");
            if (raw instanceof List<?> list) {
                List<Map<String,Object>> result = new ArrayList<>();
                for (Object itm : list) {
                    if (itm instanceof Map<?,?> mp) {
                        var casted = new HashMap<String,Object>();
                        mp.forEach((k,v)-> casted.put(k.toString(), v));
                        result.add(casted);
                    }
                }
                return result;
            }
        }
        return List.of();
    }

    public boolean joinGame(int gameID, String color) {
        Map<String,Object> body = new HashMap<>();
        body.put("gameID",(double)gameID);
        if (color!=null) {
            body.put("playerColor", color.toUpperCase());
        }
        var resp = doCall("PUT","/game", body);
        return !resp.containsKey("Error") && !resp.containsKey("message");
    }

    public void clear() {
        doCall("DELETE", "/db", null);
    }


    private Map<String,Object> doCall(String method, String endpoint, Object payload) {
        Map<String,Object> out = new HashMap<>();
        try {
            URI uri = new URI(baseUrl + endpoint);
            HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
            http.setRequestMethod(method);

            if (authToken!=null) {
                http.setRequestProperty("Authorization", authToken);
            }
            if (payload!=null) {
                http.setDoOutput(true);
                http.setRequestProperty("Content-Type","application/json");
            }
            http.connect();

            if (payload!=null) {
                String json = new Gson().toJson(payload);
                try(OutputStream os = http.getOutputStream()) {
                    os.write(json.getBytes());
                }
            }

            int code = http.getResponseCode();
            if (code >= 400) {
                try (InputStream err = http.getErrorStream()) {
                    if (err != null && new Gson().fromJson(new InputStreamReader(err), Map.class) instanceof Map eMap) {
                        out.putAll(eMap);
                    }
                }
                out.put("Error", "HTTP " + code);
                return out;
            }
            try(InputStream in = http.getInputStream()) {
                var parsed = new Gson().fromJson(new InputStreamReader(in), Map.class);
                if (parsed!=null) {
                    out.putAll(parsed);
                }
            }
            return out;

        } catch (URISyntaxException | IOException e) {
            out.put("Error", e.getMessage());
            return out;
        }
    }
}
