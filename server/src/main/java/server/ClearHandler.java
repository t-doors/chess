package server;

import dataAccess.DataAccessException;
import service.ClearService;
import spark.Request;
import spark.Response;

public class ClearHandler {

    private final ClearService clearService;

    public ClearHandler(ClearService service) {
        this.clearService = service;
    }

    public Object handleClear(Request req, Response res) {
        try {
            clearService.clearAll();
            res.status(200);
            return "{}";
        } catch (DataAccessException e) {

            res.status(500);
            return String.format("{ \"message\" : \"Error: %s\" }", e.getMessage());
        }
    }
}
