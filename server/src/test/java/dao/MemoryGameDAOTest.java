package dao;

import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

public class MemoryGameDAOTest {

    private MemoryGameDAO gameDAO;

    @BeforeEach
    void setup() {
        gameDAO = new MemoryGameDAO();
    }

    @Test
    @DisplayName("createGame - Positive")
    void createGamePositive() throws DataAccessException {
        GameData gd = new GameData(101, null, null, "TestGame", null);
        gameDAO.createGame(gd);
        GameData fromDAO = gameDAO.getGame(101);
        assertNotNull(fromDAO);
        assertEquals("TestGame", fromDAO.gameName());
    }

    @Test
    @DisplayName("createGame - Negative (duplicate ID)")
    void createGameNegative() throws DataAccessException {
        gameDAO.createGame(new GameData(202, null, null, "Game202", null));
        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(new GameData(202, null, null, "Another202", null));
        });
    }


    @Test
    @DisplayName("getGame - Positive")
    void getGamePositive() throws DataAccessException {
        gameDAO.createGame(new GameData(303, "alice", "bob", "Game303", null));
        GameData fromDAO = gameDAO.getGame(303);
        assertEquals("Game303", fromDAO.gameName());
        assertEquals("alice", fromDAO.whiteUsername());
    }

    @Test
    @DisplayName("getGame - Negative (not found)")
    void getGameNegative() {
        assertThrows(DataAccessException.class, () -> {
            gameDAO.getGame(404);
        });
    }

    @Test
    @DisplayName("listGames - Positive")
    void listGamesPositive() throws DataAccessException {
        Collection<GameData> initial = gameDAO.listGames();
        assertNotNull(initial);
        assertEquals(0, initial.size());

        gameDAO.createGame(new GameData(1, null, null, "A", null));
        gameDAO.createGame(new GameData(2, null, null, "B", null));

        Collection<GameData> after = gameDAO.listGames();
        assertEquals(2, after.size());
    }

    @Test
    @DisplayName("listGames - Negative scenario (we may not have one, so we do a lil check)")
    void listGamesNegative() {
        assertDoesNotThrow(() -> gameDAO.listGames());
    }

    @Test
    @DisplayName("updateGame - Positive")
    void updateGamePositive() throws DataAccessException {
        gameDAO.createGame(new GameData(777, "whiteUser", null, "OriginalName", null));
        GameData updated = new GameData(777, "whiteUser", "blackUser", "UpdatedName", null);
        gameDAO.updateGame(updated);

        GameData fromDAO = gameDAO.getGame(777);
        assertEquals("blackUser", fromDAO.blackUsername());
        assertEquals("UpdatedName", fromDAO.gameName());
    }

    @Test
    @DisplayName("updateGame - Negative (no existing game)")
    void updateGameNegative() {
        GameData nonExistent = new GameData(999, "W", "B", "NonExistent", null);
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(nonExistent));
    }


    @Test
    @DisplayName("clear - Positive")
    void clearPositive() throws DataAccessException {
        gameDAO.createGame(new GameData(123, null, null, "Game123", null));
        gameDAO.clear();
        assertThrows(DataAccessException.class, () -> {
            gameDAO.getGame(123);
        });
    }

    @Test
    @DisplayName("clear - Negative")
    void clearNegative() {
        assertDoesNotThrow(() -> gameDAO.clear());
    }
}
