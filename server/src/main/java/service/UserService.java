package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import java.util.UUID;


public class UserService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;


    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }


    public AuthData registerUser(UserData user) throws DataAccessException, BadRequestException, UserAlreadyExistsException {

        if (user.username() == null || user.username().isEmpty()
                || user.password() == null || user.password().isEmpty()) {
            throw new BadRequestException("username or password was empty");
        }

        try {
            userDAO.getUser(user.username());
            throw new UserAlreadyExistsException("User already taken: " + user.username());
        } catch (DataAccessException e) {

        }

        userDAO.createUser(user);
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(user.username(), token);
        authDAO.createAuth(authData);

        return authData;
    }

    public AuthData loginUser(UserData loginUser) throws DataAccessException, UnauthorizedException {
        if (loginUser.username() == null || loginUser.username().isEmpty() ||
                loginUser.password() == null || loginUser.password().isEmpty()) {
            throw new UnauthorizedException("Missing username or password");
        }

        UserData existingUser = userDAO.getUser(loginUser.username());

        if (!existingUser.password().equals(loginUser.password())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(existingUser.username(), token);
        authDAO.createAuth(authData);

        return authData;
    }

    public void logoutUser(String authToken) throws DataAccessException, UnauthorizedException {
        if (authToken == null || authToken.isEmpty()) {
            throw new UnauthorizedException("No authToken provided");
        }

        try {
            authDAO.deleteAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException("Invalid token");
        }
    }

}
