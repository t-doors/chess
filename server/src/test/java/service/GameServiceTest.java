package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

public class GameServiceTest {

    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    private final String validToken = "real-token";
    private final String invalidToken = "fake-token";

    @BeforeEach
    void setup() throws DataAccessException {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);

        authDAO.createAuth(new AuthData("player1", validToken));
    }

    @Test
    @DisplayName("listGames - Positive (valid token => empty at start)")
    void listGamesPositive() throws UnauthorizedException, DataAccessException {
        Collection<GameData> games = gameService.listGames(validToken);
        assertNotNull(games);
        assertEquals(0, games.size());
    }

    @Test
    @DisplayName("listGames - Negative (invalid token => unauthorized)")
    void listGamesNegative() {
        assertThrows(UnauthorizedException.class, () -> {
            gameService.listGames(invalidToken);
        });
    }

    @Test
    @DisplayName("createGame - Positive")
    void createGamePositive() throws UnauthorizedException, BadRequestException, DataAccessException {
        int id = gameService.createGame(validToken, "TestGame");
        assertTrue(id > 0);

        GameData gd = gameDAO.getGame(id);
        assertNotNull(gd);
        assertEquals("TestGame", gd.gameName());
    }

    @Test
    @DisplayName("createGame - Negative (invalid token)")
    void createGameNegative() {
        assertThrows(UnauthorizedException.class, () -> {
            gameService.createGame(invalidToken, "Nope");
        });
    }

    @Test
    @DisplayName("joinGame - Positive")
    void joinGamePositive() throws UnauthorizedException, BadRequestException, DataAccessException {
        int gID = gameService.createGame(validToken, "JoinMe");
        boolean success = gameService.joinGame(validToken, gID, "WHITE");
        assertTrue(success);

        GameData gd = gameDAO.getGame(gID);
        assertEquals("player1", gd.whiteUsername());
    }

    @Test
    @DisplayName("joinGame - Negative (invalid token => unauthorized)")
    void joinGameNegativeToken() {
        assertThrows(UnauthorizedException.class, () -> {
            gameService.joinGame(invalidToken, 999, "WHITE");
        });
    }

    @Test
    @DisplayName("joinGame - Negative (spot taken => false)")
    void joinGameSpotTaken() throws UnauthorizedException, BadRequestException, DataAccessException {
        int gID = gameService.createGame(validToken, "SpotTest");
        gameService.joinGame(validToken, gID, "WHITE");

        boolean secondTry = gameService.joinGame(validToken, gID, "WHITE");
        assertFalse(secondTry);
    }
}