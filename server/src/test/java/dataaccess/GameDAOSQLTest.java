package dataaccess;

import chess.ChessBoard;
import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


public class GameDAOSQLTest {

    private GameDAOSQL dao;
    private GameData sampleGame;


    @BeforeEach
    void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        dao = new GameDAOSQL();
        dao.clear();

        ChessGame cg = new ChessGame();
        ChessBoard cb = new ChessBoard();
        cb.resetBoard();
        cg.setBoard(cb);

        sampleGame = new GameData(101, "WhiteUser", "BlackUser", "SampleTitle", cg);
    }

    @Test
    @DisplayName("createGame - Positive (Insert New Game)")
    void createGamePositive() throws DataAccessException {
        dao.createGame(sampleGame);

        GameData fromDB = dao.getGame(sampleGame.gameID());
        assertNotNull(fromDB, "Should retrieve a game record after creation");
        assertEquals(sampleGame.gameID(), fromDB.gameID(), "GameID mismatch");
        assertEquals(sampleGame.whiteUsername(), fromDB.whiteUsername(), "White user mismatch");
        assertEquals(sampleGame.gameName(), fromDB.gameName(), "Game name mismatch");
        assertNotNull(fromDB.game(), "ChessGame object should not be null");
    }

    @Test
    @DisplayName("createGame - Negative (Duplicate ID => Exception)")
    void createGameNegative() throws DataAccessException {
        dao.createGame(sampleGame);
        assertThrows(DataAccessException.class, () -> dao.createGame(sampleGame));
    }


    @Test
    @DisplayName("getGame - Positive (Game Exists)")
    void getGamePositive() throws DataAccessException {
        dao.createGame(sampleGame);

        GameData found = dao.getGame(sampleGame.gameID());
        assertNotNull(found, "getGame should return a valid record");
        assertEquals(sampleGame, found, "GameData mismatch");
    }

    @Test
    @DisplayName("getGame - Negative (No Such ID => Exception)")
    void getGameNegative() {
        assertThrows(DataAccessException.class, () -> dao.getGame(sampleGame.gameID()));
    }

    @Test
    @DisplayName("listGames - Positive (Multiple Games)")
    void listGamesPositive() throws DataAccessException {
        dao.createGame(sampleGame);

        GameData secondGame = new GameData(202, "W2", "B2", "AnotherTitle", null);
        dao.createGame(secondGame);

        Collection<GameData> all = dao.listGames();
        assertEquals(2, all.size(), "Should list exactly two inserted games");
    }

    @Test
    @DisplayName("listGames - Negative (Empty => Zero Results)")
    void listGamesNegative() throws DataAccessException {
        Collection<GameData> all = dao.listGames();
        assertEquals(0, all.size(), "Should return an empty list when no games exist");
    }

    @Test
    @DisplayName("updateGame - Positive (Modify Existing)")
    void updateGamePositive() throws DataAccessException {
        dao.createGame(sampleGame);

        GameData updated = new GameData(
                sampleGame.gameID(),
                "NewWhite",
                "NewBlack",
                "NewTitle",
                sampleGame.game()
        );

        dao.updateGame(updated);

        GameData fromDB = dao.getGame(sampleGame.gameID());
        assertEquals(updated, fromDB, "Updated record should match new data");
    }

    @Test
    @DisplayName("updateGame - Negative (Non-Existent Game => Exception)")
    void updateGameNegative() {
        GameData nonExistent = new GameData(9999, "anyWhite", "anyBlack", "NoGame", null);
        assertThrows(DataAccessException.class, () -> dao.updateGame(nonExistent));
    }

    @Test
    @DisplayName("clear - Positive (Removes All Records)")
    void clearPositive() throws DataAccessException {
        dao.createGame(sampleGame);
        dao.createGame(new GameData(303, null, null, "temp", null));

        dao.clear();

        var results = dao.listGames();
        assertTrue(results.isEmpty(), "Should be empty after clear");
    }
}
