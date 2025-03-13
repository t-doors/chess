package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthDAOSQLTest {

    private AuthDAOSQL dao;

    @BeforeEach
    void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        dao = new AuthDAOSQL();
        dao.clear();
    }

    @Test
    @DisplayName("addAuth - Positive")
    void addAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("alice", "tokenXYZ");
        dao.createAuth(auth);

        AuthData fromDB = dao.getAuth("tokenXYZ");
        assertEquals(auth, fromDB, "Should retrieve the same token from DB");
    }

    @Test
    @DisplayName("addAuth - Negative (duplicate token => only one record stored)")
    void addAuthNegative() throws DataAccessException {
        AuthData auth = new AuthData("bob", "dupToken");
        dao.createAuth(auth);

        dao.createAuth(auth); // same token again
        AuthData fromDB = dao.getAuth("dupToken");
        assertEquals("bob", fromDB.username());
    }

    @Test
    @DisplayName("getAuth - Positive")
    void getAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("charlie", "charToken");
        dao.createAuth(auth);
        AuthData result = dao.getAuth("charToken");
        assertEquals(auth, result);
    }

    @Test
    @DisplayName("getAuth - Negative (missing token => error)")
    void getAuthNegative() {
        assertThrows(DataAccessException.class, () -> dao.getAuth("missingToken"));
    }

    @Test
    @DisplayName("deleteAuth - Positive")
    void deleteAuthPositive() throws DataAccessException {
        AuthData auth = new AuthData("david", "deleteMe");
        dao.createAuth(auth);
        dao.deleteAuth("deleteMe");
        assertThrows(DataAccessException.class, () -> dao.getAuth("deleteMe"));
    }

    @Test
    @DisplayName("deleteAuth - Negative (no record => no throw)")
    void deleteAuthNegative() {
        assertDoesNotThrow(() -> dao.deleteAuth("unknown"));
    }

    @Test
    @DisplayName("clear - Positive")
    void clearPositive() throws DataAccessException {
        dao.createAuth(new AuthData("eve", "tokenE"));
        dao.clear();
        assertThrows(DataAccessException.class, () -> dao.getAuth("tokenE"));
    }
}
