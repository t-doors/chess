package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();

        userService = new UserService(userDAO, authDAO);
    }


    @Test
    @DisplayName("registerUser - Positive (valid new user)")
    void registerUserPositive() throws DataAccessException {
        try {
            UserData newUser = new UserData("alice", "secret", "alice@example.com");
            AuthData result = userService.registerUser(newUser);

            assertNotNull(result, "Expected a non-null AuthData");
            assertEquals("alice", result.username(), "Usernames should match");
            assertNotNull(result.authToken(), "Auth token should not be null");

            UserData fromDao = userDAO.getUser("alice");
            assertNotNull(fromDao, "UserDAO should have 'alice'");
            assertEquals("secret", fromDao.password(), "Passwords should match");
            assertEquals("alice@example.com", fromDao.email(), "Emails should match");

        } catch (BadRequestException | UserAlreadyExistsException e) {
            fail("Should not throw an exception for a valid user");
        }
    }

    @Test
    @DisplayName("registerUser - Negative (already taken)")
    void registerUserNegative() throws DataAccessException {
        userDAO.createUser(new UserData("bob", "bobpw", "bob@mail.com"));

        UserData duplicate = new UserData("bob", "diffpw", "newbob@mail.com");
        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.registerUser(duplicate);
        });
    }


    @Test
    @DisplayName("registerUser - Negative (empty username => bad request)")
    void registerUserEmptyUsername() {
        UserData user = new UserData("", "pw", "some@mail.com");
        assertThrows(BadRequestException.class, () -> {
            userService.registerUser(user);
        });
    }

}
