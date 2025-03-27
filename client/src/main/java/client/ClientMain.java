package client;

import ui.ConsoleUI;

public class ClientMain {
    public static void main(String[] args) {
        String baseUrl = "http://localhost:8080";
        if (args.length == 1) {
            baseUrl = args[0];
        }
        System.out.println("Using server: " + baseUrl);

        ServerAccess server = new ServerAccess(baseUrl);

        ConsoleUI ui = new ConsoleUI(server);
        ui.run();

        System.out.println("Done.");
    }
}
