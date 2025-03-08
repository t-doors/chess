package dao;

import dataaccess.*;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class MemoryUserDAOTest {

    private MemoryUserDAO userDAO;

    @BeforeEach
    void setup() {
        userDAO = new MemoryUserDAO();
    }

    @Test
    @DisplayName("createUser - Positive")
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("alice", "pass123", "alice@mail.com");
        userDAO.createUser(user);
        UserData fromDao = userDAO.getUser("alice");
        assertEquals("alice", fromDao.username());
    }

    @Test
    @DisplayName("createUser - Negative (duplicate user)")
    void createUserNegative() throws DataAccessException {
        userDAO.createUser(new UserData("bob", "abc", "bob@mail.com"));
        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(new UserData("bob", "def", "bob2@mail.com"));
        });
    }

    @Test
    @DisplayName("getUser - Positive")
    void getUserPositive() throws DataAccessException {
        userDAO.createUser(new UserData("carol", "pw", "carol@mail.com"));
        UserData carol = userDAO.getUser("carol");
        assertNotNull(carol);
        assertEquals("carol", carol.username());
    }

    @Test
    @DisplayName("getUser - Negative (not found)")
    void getUserNegative() {
        assertThrows(DataAccessException.class, () -> userDAO.getUser("missingUser"));
    }

    @Test
    @DisplayName("clear - Positive")
    void clearPositive() throws DataAccessException {
        userDAO.createUser(new UserData("eve", "pw", "eve@mail.com"));
        userDAO.clear();
        assertThrows(DataAccessException.class, () -> userDAO.getUser("eve"));
    }

    @Test
    @DisplayName("clear - Negative")
    void clearNegative() {
        assertDoesNotThrow(() -> userDAO.clear());
    }
}
