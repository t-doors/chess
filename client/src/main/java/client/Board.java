package client;

import static ui.EscapeSequences.*;

public class Board {

    public void drawChessBoard(boolean blackView) {
        System.out.print(ERASE_SCREEN);

        int startCol, endCol, colStep;
        boolean whiteView = !blackView;

        if (!blackView) {
            startCol = 1;
            endCol = 8;
            colStep = 1;
        } else {
            startCol = 8;
            endCol = 1;
            colStep = -1;
        }

        for (int row = 8; row >= 1; row--) {
            int label = whiteView ? row : (9 - row);
            printRow(row, label, startCol, endCol, colStep, whiteView);
        }
        printColumns(whiteView);

        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void printRow(int boardRow, int label, int startCol, int endCol, int step, boolean whiteView) {
        System.out.printf("%2d ", label);
        for (int col = startCol; col != endCol + step; col += step) {
            printSquare(boardRow, col, whiteView);
        }
        System.out.println(RESET_BG_COLOR);
    }

    private void printColumns(boolean whiteView) {
        System.out.print("   ");
        if (whiteView) {
            for (char c = 'a'; c <= 'h'; c++) {
                System.out.printf(" %c ", c);
            }
        } else {
            for (char c = 'h'; c >= 'a'; c--) {
                System.out.printf(" %c ", c);
            }
        }
        System.out.println();
    }

    private void printSquare(int row, int col, boolean whiteView) {
        boolean dark;
        if (whiteView) {
            dark = ((row + col) % 2 == 0);
        } else {
            dark = ((row + col) % 2 == 1);
        }
        System.out.print(dark ? SET_BG_COLOR_MAGENTA : SET_BG_COLOR_LIGHT_GREY);

        String piece = getPiece(row, col, whiteView);
        String textColor = getTextColor(piece);
        String cell = piece.isEmpty() ? "   " : " " + piece + " ";
        System.out.print(textColor + cell + RESET_TEXT_COLOR);
    }

    private String getPiece(int r, int c, boolean isWhitePerspective) {
        int actualRow = isWhitePerspective ? r : 9 - r;
        if (actualRow == 1) {
            return switch (c) {
                case 1, 8 -> "R";
                case 2, 7 -> "N";
                case 3, 6 -> "B";
                case 4 -> "Q";
                case 5 -> "K";
                default -> "";
            };
        }
        if (actualRow == 2) {
            return "P";
        }
        if (actualRow == 7) {
            return "p";
        }
        if (actualRow == 8) {
            return switch (c) {
                case 1, 8 -> "r";
                case 2, 7 -> "n";
                case 3, 6 -> "b";
                case 4 -> "q";
                case 5 -> "k";
                default -> "";
            };
        }
        return "";
    }

    private String getTextColor(String piece) {
        if (piece.isEmpty()) {
            return RESET_TEXT_COLOR;
        }
        return Character.isUpperCase(piece.charAt(0))
                ? SET_TEXT_COLOR_WHITE
                : SET_TEXT_COLOR_BLACK;
    }
}