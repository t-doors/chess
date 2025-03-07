package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.*;
import service.*;
import spark.*;

/**
 * Handles incoming requests to POST /user (user registration).
 */
public class UserHandler {

    private final UserService userService;
    private final Gson gson;

    public UserHandler(UserService userService) {
        this.userService = userService;
        this.gson = new Gson();
    }

    public Object handleRegister(Request req, Response res) {
        try {
            UserData requestUser = gson.fromJson(req.body(), UserData.class);

            AuthData authData = userService.registerUser(requestUser);

            res.status(200);
            return gson.toJson(authData);

        } catch (BadRequestException e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";

        } catch (UserAlreadyExistsException e) {
            res.status(403);
            return "{ \"message\": \"Error: already taken\" }";

        } catch (DataAccessException e) {

            res.status(500);
            return String.format("{ \"message\": \"Error: %s\"}", e.getMessage());

        } catch (Exception e) {

            res.status(500);
            return String.format("{ \"message\": \"Error: %s\"}", e.getMessage());
        }
    }
}
