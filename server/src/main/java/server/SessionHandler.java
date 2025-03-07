package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import service.UserService;
import spark.*;


public class SessionHandler {

    private final UserService userService;
    private final Gson gson = new Gson();

    public SessionHandler(UserService userService) {
        this.userService = userService;
    }

    public Object handleLogin(Request req, Response res) {
        try {
            UserData loginRequest = gson.fromJson(req.body(), UserData.class);

            AuthData authData = userService.loginUser(loginRequest);

            res.status(200);
            return gson.toJson(authData);

        } catch (UnauthorizedException e) {
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        } catch (DataAccessException e) {
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        } catch (Exception e) {
            res.status(500);
            return String.format("{ \"message\": \"Error: %s\"}", e.getMessage());
        }
    }
    public Object handleLogout(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            userService.logoutUser(authToken);

            res.status(200);
            return "{}";
        } catch (UnauthorizedException e) {
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        } catch (DataAccessException e) {
            res.status(500);
            return String.format("{ \"message\": \"Error: %s\"}", e.getMessage());
        }
    }

}
