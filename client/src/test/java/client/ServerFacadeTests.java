package client;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static ServerAccess access;

    @BeforeAll
    static void init() {

        int port = 8080;

        access = new ServerAccess("http://localhost:" + port);

        System.out.println("ServerAccessTest init: using port " + port);
    }


    @Test
    @Order(1)
    void testRegisterPositive() {
        boolean ok = access.register("tester","pw","test@mail");
        assertTrue(ok, "Should register a new user successfully");
    }

    @Test
    @Order(2)
    void testRegisterNegative() {
        access.register("bob","pw","bob@x");
        boolean ok2 = access.register("bob","pw","bob@x");
        assertFalse(ok2, "Registering same username again should fail");
    }

    @Test
    @Order(3)
    void testLoginPositive() {
        access.register("alice","pw","a@x");
        assertTrue(access.login("alice","pw"), "alice should login with correct password");
    }

    @Test
    @Order(4)
    void testLoginNegative() {
        assertFalse(access.login("unknown","none"), "unknown user should fail to login");
    }

    @Test
    @Order(5)
    void testLogoutPositive() {
        access.register("logoutUser","pass","logout@ex");
        assertTrue(access.login("logoutUser","pass"), "should login prior to logout");
        assertTrue(access.logout(), "should succeed in logging out");
    }

    @Test
    @Order(6)
    void testLogoutNegative() {
        assertFalse(access.logout(), "logout without an active session should fail or return false");
    }

    @Test
    @Order(7)
    void testCreateGamePositive() {
        access.register("creator","pw","c@x");
        access.login("creator","pw");
        int gID = access.createGame("myCoolGame");
        assertTrue(gID >= 0, "createGame should return a valid ID");
    }

    @Test
    @Order(8)
    void testCreateGameNegative() {
        int gID = access.createGame("badGame");
        assertEquals(-1, gID, "createGame while not logged in should fail => -1");
    }

    @Test
    @Order(9)
    void testListGamesPositive() {
        access.register("listUser","pw","l@x");
        access.login("listUser","pw");
        access.createGame("someListGame");
        var list = access.listGames();
        assertFalse(list.isEmpty(), "should find at least one game after creating it");
    }

    @Test
    @Order(10)
    void testListGamesNegative() {
        var list = access.listGames();
        assertTrue(list.isEmpty(), "no auth => listGames fails => returns empty");
    }


    @Test
    @Order(11)
    void testJoinGamePositive() {
        access.register("joiner","jj","join@j");
        access.login("joiner","jj");
        int gameID = access.createGame("joinable");
        assertTrue(gameID >= 0, "create game to join");
        boolean joined = access.joinGame(gameID,"WHITE");
        assertTrue(joined, "should be able to join newly created game as WHITE");
    }

    @Test
    @Order(12)
    void testJoinGameNegative() {
        int someGameID = 123456;
        assertFalse(access.joinGame(someGameID,"BLACK"), "joinGame with no session => fail");
    }
}
