package dataaccess;

import model.GameData;

import java.util.Collection;
import java.util.HashMap;


public class MemoryGameDAO implements GameDAO {

    private final HashMap<Integer, GameData> gameMap;

    public MemoryGameDAO() {
        gameMap = new HashMap<>();
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        if (gameMap.containsKey(game.gameID())) {
            throw new DataAccessException("Game already exists: " + game.gameID());
        }
        gameMap.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        if (!gameMap.containsKey(gameID)) {
            throw new DataAccessException("Game not found: " + gameID);
        }
        return gameMap.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() {
        return gameMap.values();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!gameMap.containsKey(game.gameID())) {
            throw new DataAccessException("Cannot update non-existent game: " + game.gameID());
        }
        gameMap.put(game.gameID(), game);
    }

    @Override
    public void clear() {
        gameMap.clear();
    }
}
