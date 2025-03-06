package server;

import com.google.gson.Gson;
import dataAccess.*;
import model.GameData;
import service.GameService;
import spark.*;

import java.util.Collection;

public class GameHandler {

    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public Object handleListGames(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");

            Collection<GameData> allGames = gameService.listGames(authToken);

            res.status(200);
            return "{ \"games\": " + gson.toJson(allGames) + " }";

        } catch (UnauthorizedException e) {
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        } catch (DataAccessException e) {
            res.status(500);
            return String.format("{ \"message\": \"Error: %s\" }", e.getMessage());
        } catch (Exception e) {
            res.status(500);
            return String.format("{ \"message\": \"Error: %s\" }", e.getMessage());
        }
    }
}
