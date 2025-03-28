package server;

import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import service.GameService;
import spark.*;

import java.util.Collection;
import java.util.Map;

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

            int gameID = gameService.createGame(authToken, createReq.gameName());

            res.status(200);
            return String.format("{ \"gameID\": %d }", gameID);

        } catch (BadRequestException e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";

        } catch (UnauthorizedException e) {
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";

        } catch (DataAccessException e) {
            res.status(500);
            return "{ \"message\": \"Error: %s\" }".formatted(e.getMessage());

        } catch (Exception e) {
            res.status(500);
            return "{ \"message\": \"Error: %s\" }".formatted(e.getMessage());
        }
    }

    public Object handleObserve(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            String gameIDParam = req.queryParams("gameID");

            if (gameIDParam == null) {
                res.status(400);
                return new Gson().toJson(Map.of("message", "Error: bad request"));
            }

            int gameID = Integer.parseInt(gameIDParam);

            gameService.observeGame(authToken, gameID);
            res.status(200);
            return "";
        } catch (NumberFormatException e) {
            res.status(400);
            return new Gson().toJson(Map.of("message", "Error: Invalid game ID format"));
        } catch (UnauthorizedException e) {
            res.status(401);
            return new Gson().toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (BadRequestException e) {
            res.status(400);
            return new Gson().toJson(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }


    public Object handleJoinGame(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");

            record JoinGameRequest(String playerColor, int gameID) {}
            JoinGameRequest body = new Gson().fromJson(req.body(), JoinGameRequest.class);

            boolean success = gameService.joinGame(authToken, body.gameID(), body.playerColor());

            if (success) {
                res.status(200);
                return "{}";
            } else {
                res.status(403);
                return "{ \"message\": \"Error: already taken\" }";
            }
        } catch (BadRequestException e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        } catch (UnauthorizedException e) {
            res.status(401);
            return "{ \"message\": \"Error: unauthorized\" }";
        } catch (DataAccessException e) {
            res.status(400);
            return "{ \"message\": \"Error: bad request\" }";
        } catch (Exception e) {
            res.status(500);
            return String.format("{ \"message\": \"Error: %s\"}", e.getMessage());
        }
    }




}
