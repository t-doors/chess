package ui;

import java.util.Scanner;

public class ConsoleUI {

    private enum State { PRELOGIN, POSTLOGIN, EXIT }

    private State state;
    private final Scanner scanner;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.state = State.PRELOGIN;
    }

    public void run() {
        System.out.println("Welcome to Chess amigo.");
        while (state != State.EXIT) {
            switch (state) {
                case PRELOGIN -> doPrelogin();
                default -> state = State.EXIT;
            }
        }
    }

    private void doPrelogin() {
        System.out.print("\n[prelogin] >>> ");
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) return;
        String[] tokens = line.split("\\s+");
        String cmd = tokens[0].toLowerCase();

        switch(cmd) {
            case "help" -> System.out.println("prelogin commands: register <u> <p> <e>, login <u> <p>, quit");
            case "quit" -> state = State.EXIT;
            case "register" -> System.out.println("Stub register...");
            case "login" -> System.out.println("Stub login...");
            default -> System.out.println("Unrecognized. 'help' or 'quit'.");
        }
    }
}
