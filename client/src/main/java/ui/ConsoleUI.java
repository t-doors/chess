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
        System.out.println("Welcome to Chess. Type 'help' for usage.");
        while (state != State.EXIT) {
            switch (state) {
                case PRELOGIN -> handlePrelogin();
                case POSTLOGIN -> handlePostlogin();
            }
        }
        System.out.println("Exiting console UI. Adios.");
    }

    private void handlePrelogin() {
        System.out.print("\n[prelogin] >>> ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
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
                    System.out.println("Register failed (username in use?).");
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
                    System.out.println("Login failed (user not found or wrong pass).");
                }
            }
            default -> System.out.println("Unknown command. 'help' or 'quit'.");
        }
    }

    private void printPreloginHelp() {
        System.out.println("""
            Prelogin commands:
              register <USER> <PASS> <EMAIL>
              login <USER> <PASS>
              quit
              help
        """);
    }

    private void handlePostlogin() {
        System.out.print("\n[postlogin] >>> ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        String[] tokens = line.split("\\s+",2);
        String cmd = tokens[0].toLowerCase();

        switch (cmd) {
            case "help" -> printPostloginHelp();
            case "quit" -> state = State.EXIT;
            case "logout" -> {
                boolean ok = server.logout();
                if (!ok) {
                    System.out.println("Logout might have failed or no session. Returning to prelogin anyway.");
                }
                state = State.PRELOGIN;
            }
            case "create" -> doCreate(tokens);
            case "list" -> doList();
            case "join" -> doJoin(line);
            case "observe" -> doObserve(line);
            default -> System.out.println("Unknown postlogin command. 'help' or 'quit'.");
        }
    }

    private void printPostloginHelp() {
        System.out.println("""
            Postlogin commands:
              create <NAME>           (create a new game)
              list                    (list all games)
              join <INDEX> <COLOR>    (join a game as WHITE or BLACK)
              observe <INDEX>         (observe a game from White's perspective)
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
            System.out.println("Created game '" + gameName + "' with ID=" + gID);
        } else {
            System.out.println("Create game failed (maybe unauthorized?).");
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
            System.out.printf("%2d) gameID=%d, name=%s, white=%s, black=%s%n", i+1, gameID, name, w, b);
        }
    }

    private void doJoin(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 3) {
            System.out.println("Usage: join <LIST_INDEX> <WHITE|BLACK>");
            return;
        }
        int idx;
        try { idx = Integer.parseInt(parts[1]); }
        catch (NumberFormatException e) {
            System.out.println("join: invalid index");
            return;
        }
        if (idx<1 || idx>cachedGames.size()) {
            System.out.println("Index out of range. 'list' first?");
            return;
        }
        String color = parts[2];
        double d = (double) cachedGames.get(idx-1).get("gameID");
        int gameID = (int)d;
        boolean ok = server.joinGame(gameID,color);
        if (!ok) {
            System.out.println("join failed (color taken or invalid?).");
            return;
        }
        System.out.println("Joined game as " + color + ". Let's show the board:");
        boolean blackView = color.equalsIgnoreCase("black");
        new Board().drawChessBoard(blackView);
    }

    private void doObserve(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length < 2) {
            System.out.println("Usage: observe <LIST_INDEX>");
            return;
        }
        int idx;
        try { idx = Integer.parseInt(parts[1]); }
        catch (NumberFormatException e) {
            System.out.println("Invalid index for observe");
            return;
        }
        if (idx<1 || idx>cachedGames.size()) {
            System.out.println("Index out of range. 'list' first?");
            return;
        }
        double d = (double) cachedGames.get(idx-1).get("gameID");
        int gameID = (int)d;
        boolean ok = server.joinGame(gameID,null);
        if (!ok) {
            System.out.println("observe failed or game not found");
            return;
        }
        System.out.println("Observing from White's perspective:");
        new Board().drawChessBoard(false);
    }
}
