package service;

import com.google.gson.Gson;
import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import server.GameHandler;
import spark.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class GameHandlerTest {

    private GameHandler gameHandler;
    private DummyGameService dummyGameService;
    private TestRequest req;
    private TestResponse res;
    private Gson gson = new Gson();

    @BeforeEach
    void setup() {
        Collection<GameData> dummyGames = new ArrayList<>();
        dummyGames.add(new GameData(123, null, null, "TestGame", null));
        dummyGameService = new DummyGameService(dummyGames);
        gameHandler = new GameHandler(dummyGameService);

        req = new TestRequest();
        res = new TestResponse();
    }

    // --- Test for handleListGames (Positive) ---
    @Test
    @DisplayName("handleListGames - Positive")
    void handleListGamesPositive() {
        req.setHeader("authorization", "validToken");
        dummyGameService.setThrowUnauthorized(false);

        Object result = gameHandler.handleListGames(req, res);

        assertEquals(200, res.getStatus(), "Expected HTTP status 200");

        String json = result.toString();
        assertTrue(json.contains("\"gameID\":123"), "JSON should contain gameID 123");
        assertTrue(json.contains("TestGame"), "JSON should contain gameName 'TestGame'");
    }

    @Test
    @DisplayName("handleListGames - Negative (bad token => unauthorized)")
    void handleListGamesNegative() {
        req.setHeader("authorization", "fakeToken");
        dummyGameService.setThrowUnauthorized(true);

        Object result = gameHandler.handleListGames(req, res);

        assertEquals(401, res.getStatus(), "Expected HTTP status 401 for bad token");

        String json = result.toString();
        assertTrue(json.contains("Error: unauthorized"), "Expected unauthorized error message");
    }

    private static class DummyGameService extends GameService {
        private Collection<GameData> dummyGames;
        private boolean throwUnauthorized = false;

        public DummyGameService(Collection<GameData> dummyGames) {
            super(null, null);
            this.dummyGames = dummyGames;
        }

        public void setThrowUnauthorized(boolean value) {
            this.throwUnauthorized = value;
        }

        @Override
        public Collection<GameData> listGames(String authToken) throws UnauthorizedException {
            if (throwUnauthorized) {
                throw new UnauthorizedException("Invalid token");
            }
            return dummyGames;
        }
    }

    private static class TestRequest extends Request {
        private Map<String, String> headerMap = new HashMap<>();
        private String body = "";

        public void setHeader(String key, String value) {
            headerMap.put(key, value);
        }

        @Override
        public String headers(String header) {
            return headerMap.get(header);
        }

        public void setBody(String body) {
            this.body = body;
        }

        @Override
        public String body() {
            return body;
        }
    }

    private static class TestResponse extends Response {
        private int status;
        private String body;

        @Override
        public void status(int statusCode) {
            this.status = statusCode;
        }

        public int getStatus() {
            return status;
        }

        @Override
        public void body(String body) {
            this.body = body;
        }

        public String getBody() {
            return body;
        }
    }
}
