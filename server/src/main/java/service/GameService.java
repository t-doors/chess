package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService {

    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public Collection<GameData> listGames(String authToken)
            throws UnauthorizedException, DataAccessException {

        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException("Missing authToken");
        }

        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException ex) {
            throw new UnauthorizedException("Invalid token");
        }

        return gameDAO.listGames();
    }

    public int createGame(String authToken, String gameName)
            throws UnauthorizedException, BadRequestException, DataAccessException {
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException ex) {
            throw new UnauthorizedException("Invalid token");
        }

        if (gameName == null || gameName.isEmpty()) {
            throw new BadRequestException("No gameName provided.");
        }

        int gameID;
        do {
            gameID = (int) (Math.random() * 9999) + 1;

        } while (gameDAOAlreadyHas(gameID));

        GameData newGame = new GameData(gameID, null, null, gameName, null);
        gameDAO.createGame(newGame);

        return gameID;
    }

    private boolean gameDAOAlreadyHas(int gameID) {
        try {
            gameDAO.getGame(gameID);
            return true;
        } catch (DataAccessException ex) {
            return false;
        }
    }

    public boolean joinGame(String authToken, int gameID, String requestedColor)
            throws UnauthorizedException, DataAccessException, BadRequestException {

        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException("No token provided");
        }
        AuthData authData;
        try {
            authData = authDAO.getAuth(authToken);
        } catch (DataAccessException ex) {
            throw new UnauthorizedException("Invalid token");
        }

        GameData game;
        try {
            game = gameDAO.getGame(gameID);
        } catch (DataAccessException ex) {
            throw new BadRequestException("Game not found: " + gameID);
        }

        String username = authData.username();
        String currentWhite = game.whiteUsername();
        String currentBlack = game.blackUsername();

        if (requestedColor == null || requestedColor.trim().isEmpty()) {
            throw new BadRequestException("Error: must use team color (black or white)");
        }

        String color = requestedColor.toUpperCase();
        if (!"WHITE".equals(color) && !"BLACK".equals(color)) {
            throw new BadRequestException("Error: invalid team color");
        }

        if ("WHITE".equals(color)) {
            if (currentWhite != null && !currentWhite.equals(username)) {
                return false;
            }
            currentWhite = username;
        } else {
            if (currentBlack != null && !currentBlack.equals(username)) {
                return false;
            }
            currentBlack = username;
        }

        GameData updated = new GameData(game.gameID(), currentWhite, currentBlack, game.gameName(), game.game());
        gameDAO.updateGame(updated);
        return true;
    }

    public void observeGame(String authToken, int gameID)
            throws UnauthorizedException, DataAccessException, BadRequestException {
        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException("No token provided");
        }
        try {
            authDAO.getAuth(authToken);
        } catch (DataAccessException ex) {
            throw new UnauthorizedException("Invalid token");
        }
        try {
            gameDAO.getGame(gameID);
        } catch (DataAccessException ex) {
            throw new BadRequestException("Game not found: " + gameID);
        }
    }

}
