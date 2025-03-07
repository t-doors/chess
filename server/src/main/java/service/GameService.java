package service;

import dataAccess.*;
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

        authDAO.getAuth(authToken);

        return gameDAO.listGames();
    }

    public int createGame(String authToken, String gameName)
            throws UnauthorizedException, BadRequestException, DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException("No auth token provided.");
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
            throws UnauthorizedException, DataAccessException, BadRequestException {

        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException("No auth token found");
        }
        AuthData authData = authDAO.getAuth(authToken);

        GameData game = gameDAO.getGame(gameID);

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
