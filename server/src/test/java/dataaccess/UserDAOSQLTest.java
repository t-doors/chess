package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOSQLTest {

    private UserDAOSQL dao;

    @BeforeEach
    void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        dao = new UserDAOSQL();
        dao.clear();
    }

    @Test
    @DisplayName("createUser - Positive")
    void createUserPositive() throws DataAccessException {
        UserData user = new UserData("alice", "alicePass", "alice@mail.com");
        dao.createUser(user);

        UserData fromDB = dao.getUser("alice");
        assertEquals("alice", fromDB.username());
        assertEquals("alice@mail.com", fromDB.email());
    }

    @Test
    @DisplayName("createUser - Negative (duplicate username => error)")
    void createUserNegative() throws DataAccessException {
        dao.createUser(new UserData("bob","bobPass","bob@mail.com"));
        assertThrows(DataAccessException.class, () -> {
            dao.createUser(new UserData("bob","another","mail2@mail.com"));
        });
    }

    @Test
    @DisplayName("getUser - Positive")
    void getUserPositive() throws DataAccessException {
        dao.createUser(new UserData("charlie","charPw","c@mail.com"));
        UserData c = dao.getUser("charlie");
        assertEquals("charlie", c.username());
        assertEquals("c@mail.com", c.email());
    }

    @Test
    @DisplayName("getUser - Negative (not found)")
    void getUserNegative() {
        assertThrows(DataAccessException.class, () -> dao.getUser("nofound"));
    }


    @Test
    @DisplayName("clear - Positive")
    void clearPositive() throws DataAccessException {
        dao.createUser(new UserData("frank","frankPw","f@mail.com"));
        dao.clear();
        assertThrows(DataAccessException.class, () -> dao.getUser("frank"));
    }
}
