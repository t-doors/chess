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
    @DisplayName("loginUser - Positive (correct credentials)")
    void loginUserPositive() throws DataAccessException, UserAlreadyExistsException, BadRequestException {
        userService.registerUser(new UserData("charlie", "charpw", "char@mail.com"));

        try {
            AuthData authResult = userService.loginUser(new UserData("charlie", "charpw", "char@mail.com"));

            assertNotNull(authResult, "AuthData should not be null on success");
            assertEquals("charlie", authResult.username(), "Usernames should match");
            assertNotNull(authResult.authToken(), "Token should not be null");
        } catch (UnauthorizedException e) {
            fail("Should not throw UnauthorizedException for correct credentials");
        }
    }

    @Test
    @DisplayName("loginUser - Negative (wrong password => unauthorized)")
    void loginUserNegative() throws DataAccessException, UserAlreadyExistsException, BadRequestException {
        userService.registerUser(new UserData("david", "davidpw", "david@mail.com"));

        UserData badPass = new UserData("david", "incorrect", "david@mail.com");
        assertThrows(UnauthorizedException.class, () -> {
            userService.loginUser(badPass);
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
