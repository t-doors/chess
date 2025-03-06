package service;

import dataAccess.*;
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
}
