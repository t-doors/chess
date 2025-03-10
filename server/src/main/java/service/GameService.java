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
        authDAO.getAuth(authToken);

        if (gameName == null || gameName.isEmpty()) {
            throw new BadRequestException("No gameName provided.");
        }

        int gameID;
        do {
            gameID = (int) (Math.random() * 9999) + 1;

        } while (gameDAOAlreadyHas(gameID));


        GameData newGame = new GameData(gameID,
                null, null,
                gameName, null);

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
            throws UnauthorizedException, DataAccessException, BadRequestException{

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

        if (requestedColor == null) {
            throw new BadRequestException("No color specified");
        }
        String color = requestedColor.toUpperCase();
        if (!"WHITE".equals(color) && !"BLACK".equals(color)) {
            throw new BadRequestException("Invalid color: " + requestedColor);
        }

        String whiteUser = game.whiteUsername();
        String blackUser = game.blackUsername();

        if ("WHITE".equals(color)) {
            if (whiteUser != null) {
                return false;
            }
            whiteUser = authData.username();
        } else {
            if (blackUser != null) {
                return false;
            }
            blackUser = authData.username();
        }

        GameData updated = new GameData(
                game.gameID(),
                whiteUser,
                blackUser,
                game.gameName(),
                game.game()
        );
        gameDAO.updateGame(updated);

        return true;
    }

}
