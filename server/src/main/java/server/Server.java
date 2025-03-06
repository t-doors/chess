package server;

import dataAccess.*;
import service.*;
import spark.*;

public class Server {
    private ClearService clearService;
    private ClearHandler clearHandler;

    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    private UserService userService;
    private UserHandler userHandler;
    private SessionHandler sessionHandler;

    public Server() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        clearService = new ClearService(userDAO, gameDAO, authDAO);
        clearHandler = new ClearHandler(clearService);

        userService = new UserService(userDAO, authDAO);
        userHandler = new UserHandler(userService);

        sessionHandler = new SessionHandler(userService);

    }
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.delete("/db", clearHandler::handleClear);
        Spark.post("/user", userHandler::handleRegister);
        Spark.post("/session", sessionHandler::handleLogin);
        Spark.delete("/session", sessionHandler::handleLogout);


        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
