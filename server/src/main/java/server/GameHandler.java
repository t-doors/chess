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

    public Object handleCreateGame(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");

            record CreateGameRequest(String gameName) {}
            CreateGameRequest createReq = new Gson().fromJson(req.body(), CreateGameRequest.class);

            int newID = gameService.createGame(authToken, createReq.gameName());

            res.status(200);
            return "{ \"gameID\": %d }".formatted(newID);

        } catch (BadRequestException e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";

        } catch (UnauthorizedException e) {
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";

        } catch (DataAccessException e) {
            res.status(500);
            return "{ \"message\": \"Error: %s\"}".formatted(e.getMessage());

        } catch (Exception e) {
            res.status(500);
            return "{ \"message\": \"Error: %s\"}".formatted(e.getMessage());
        }
    }

}
