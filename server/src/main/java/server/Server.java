package server;

import dataAccess.*;
import service.ClearService;
import spark.*;

public class Server {
    private ClearService clearService;
    private ClearHandler clearHandler;

    private UserDAO userDAO;
    private GameDAO gameDAO;
    private AuthDAO authDAO;


    public Server() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        clearService = new ClearService(userDAO, gameDAO, authDAO);
        clearHandler = new ClearHandler(clearService);

    }
    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        Spark.delete("/db", clearHandler::handleClear);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
