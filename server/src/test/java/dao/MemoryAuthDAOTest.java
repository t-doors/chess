package dao;

import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;


public class MemoryAuthDAOTest {

    private MemoryAuthDAO authDAO;

    @BeforeEach
    void setup() {
        authDAO = new MemoryAuthDAO();
    }


    @Test
    @DisplayName("addAuth - Positive")
    void addAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("username1", "tokenXYZ");
        authDAO.createAuth(auth);

        AuthData result = authDAO.getAuth("tokenXYZ");
        assertNotNull(result);
        assertEquals("username1", result.username());
    }

    @Test
    @DisplayName("addAuth - Negative (duplicate token)")
    void addAuthNegative() throws DataAccessException {
        authDAO.createAuth(new AuthData("userA", "dupToken"));
        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(new AuthData("userB", "dupToken"));
        });
    }


    @Test
    @DisplayName("getAuth - Positive")
    void getAuthPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("userC", "tokenC"));
        AuthData found = authDAO.getAuth("tokenC");
        assertEquals("userC", found.username());
    }

    @Test
    @DisplayName("getAuth - Negative (not found)")
    void getAuthNegative() {
        assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("missingToken");
        });
    }


    @Test
    @DisplayName("deleteAuth - Positive")
    void deleteAuthPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("userD", "tokenD"));
        authDAO.deleteAuth("tokenD");

        assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("tokenD");
        });
    }

    @Test
    @DisplayName("deleteAuth - Negative (no such token)")
    void deleteAuthNegative() {
        assertThrows(DataAccessException.class, () -> {
            authDAO.deleteAuth("unknownToken");
        });
    }

    @Test
    @DisplayName("clear - Positive")
    void clearPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("u1", "t1"));
        authDAO.createAuth(new AuthData("u2", "t2"));
        authDAO.clear();
        assertThrows(DataAccessException.class, () -> authDAO.getAuth("t1"));
        assertThrows(DataAccessException.class, () -> authDAO.getAuth("t2"));
    }

    @Test
    @DisplayName("clear - Negative (not typically needed but we do it for coverage)")
    void clearNegative() {
        authDAO.clear();
        assertDoesNotThrow(() -> authDAO.clear());
    }
}
