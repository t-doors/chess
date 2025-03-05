package service;

import dataAccess.*;
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
}
