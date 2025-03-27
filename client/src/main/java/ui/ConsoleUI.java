package ui;

import client.Board;
import client.ServerAccess;

import java.util.*;

public class ConsoleUI {
    private enum State { PRELOGIN, POSTLOGIN, EXIT }

    private final Scanner scanner;
    private final ServerAccess server;
    private State state;
    private List<Map<String,Object>> cachedGames = List.of();

    public ConsoleUI(ServerAccess server) {
        this.scanner = new Scanner(System.in);
        this.server = server;
        this.state = State.PRELOGIN;
    }

    public void run() {
        System.out.println(" ♕ Welcome to Chess Amigo ♕ Type 'help' for instructions.");
        while (state != State.EXIT) {
            switch (state) {
                case PRELOGIN -> handlePrelogin();
                case POSTLOGIN -> handlePostlogin();
            }
        }
        System.out.println("♕ Exiting Chess UI ♕ Adios amigo.");
    }

    private void handlePrelogin() {
        System.out.print("\n[prelogin] >>> ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()){
            return;
        }
        String[] tokens = line.split("\\s+");
        String cmd = tokens[0].toLowerCase();

        switch (cmd) {
            case "help" -> printPreloginHelp();
            case "quit" -> state = State.EXIT;
            case "register" -> {
                if (tokens.length < 4) {
                    System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
                    break;
                }
                boolean ok = server.register(tokens[1], tokens[2], tokens[3]);
                if (ok) {
                    System.out.println("Registered & logged in!");
                    state = State.POSTLOGIN;
                } else {
                    System.out.println("Register failed :( Username already taken?");
                }
            }
            case "login" -> {
                if (tokens.length < 3) {
                    System.out.println("Usage: login <USERNAME> <PASSWORD>");
                    break;
                }
                boolean ok = server.login(tokens[1], tokens[2]);
                if (ok) {
                    System.out.println("Login success!");
                    state = State.POSTLOGIN;
                } else {
                    System.out.println("Login failed: user not found or wrong password :(");
                }
            }
            default -> System.out.println("Unknown command. Type 'help' or 'quit'.");
        }
    }

    private void printPreloginHelp() {
        System.out.println("""
            Prelogin commands:
              register <USERNAME> <PASSWORD> <EMAIL>
              login <USERNAME> <PASSWORD>
              quit
              help
        """);
    }

    private void handlePostlogin() {
        System.out.print("\n[postlogin] >>> ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            return;
        }
        String[] tokens = line.split("\\s+",2);
        String cmd = tokens[0].toLowerCase();

        switch (cmd) {
            case "help" -> printPostloginHelp();
            case "quit" -> state = State.EXIT;
            case "logout" -> {
                boolean ok = server.logout();
                if (!ok) {
                    System.out.println("Logout failure or no session. Returning to prelogin.");
                }
                state = State.PRELOGIN;
            }
            case "create" -> doCreate(tokens);
            case "list" -> doList();
            case "join" -> doJoin(line);
            case "observe" -> doObserve(line);
            default -> System.out.println("Unknown postlogin command. Type 'help' or 'quit'.");
        }
    }

    private void printPostloginHelp() {
        System.out.println("""
            Postlogin commands:
              create <NAME>           (create a new game)
              list                    (list all games)
              join <INDEX> <COLOR>    (join a game as WHITE or BLACK)
              observe <INDEX>         (observe a game from the White perspective)
              logout
              quit
              help
        """);
    }

    private void doCreate(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: create <GAME_NAME>");
            return;
        }
        String gameName = tokens[1];
        int gID = server.createGame(gameName);
        if (gID >= 0) {
            System.out.println("Created game '" + gameName + "'");
        } else {
            System.out.println("Failed to create game (maybe unauthorized?).");
        }
    }

    private void doList() {
        cachedGames = server.listGames();
        if (cachedGames.isEmpty()) {
            System.out.println("No games found or unauthorized. Did you login?");
            return;
        }
        System.out.println("Games:");
        for (int i=0; i< cachedGames.size(); i++) {
            Map<String,Object> g = cachedGames.get(i);
            double d = (double) g.get("gameID");
            int gameID = (int)d;
            String name = (String) g.getOrDefault("gameName","(no name)");
            String w = (String) g.getOrDefault("whiteUsername","(open)");
            String b = (String) g.getOrDefault("blackUsername","(open)");
            System.out.printf("%2d) name: %s, white: %s, black: %s%n", i+1, name, w, b);
        }
    }
    private boolean isValidIndex(int idx) {
        return idx >= 1 && idx <= cachedGames.size();
    }

    private void doJoin(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 3) {
            System.out.println("Usage: join <LIST_INDEX> <WHITE|BLACK>");
            return;
        }
        int idx;
        try {
            idx = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid index format");
            return;
        }

        if (!isValidIndex(idx)) {
            System.out.println("Error: Index out of range. Use 'list' to see valid games.");
            return;
        }

        String color = parts[2].toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")) {
            System.out.println("Error: Invalid color. Use 'WHITE' or 'BLACK'.");
            return;
        }

        Map<String, Object> game = cachedGames.get(idx - 1);
        Object gameIDObj = game.get("gameID");
        int gameID = (gameIDObj instanceof Number) ? ((Number) gameIDObj).intValue() : -1;

        if (gameID == -1) {
            System.out.println("Error: Invalid game ID");
            return;
        }

        if (!server.joinGame(gameID, color)) {
            System.out.println("Error: The color might be taken or the game doesn't exist.");
            return;
        }

        System.out.println("Joined game as " + color + ". Drawing board:");
        new Board().drawChessBoard(color.equalsIgnoreCase("BLACK"));
    }

    private void doObserve(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Usage: observe <LIST_INDEX>");
            return;
        }

        int idx;
        try {
            idx = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid index format");
            return;
        }

        if (!isValidIndex(idx)) {
            System.out.println("Error: Index out of range. Type 'list' to see valid games.");
            return;
        }

        Map<String, Object> game = cachedGames.get(idx - 1);
        Object gameIDObj = game.get("gameID");
        int gameID = (gameIDObj instanceof Number) ? ((Number) gameIDObj).intValue() : -1;

        if (gameID == -1) {
            System.out.println("Error: Invalid game ID");
            return;
        }


        if (!server.observeGame(gameID)) {
            System.out.println("Failed to observe game. It might not exist.");
            return;
        }

        System.out.println("Observing game from the White perspective:");
        new Board().drawChessBoard(false);
    }
}
