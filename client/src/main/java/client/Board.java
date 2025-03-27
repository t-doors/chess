package client;

import static ui.EscapeSequences.*;

public class Board {
    public void drawChessBoard(boolean blackView) {
        System.out.print(ERASE_SCREEN);

        if (!blackView) {
            for (int row = 8; row >= 1; row--) {
                printRowWhiteView(row);
            }
            printColumnsWhite();
        } else {
            for (int row = 8; row >= 1; row--) {
                printRowBlackView(row);
            }
            printColumnsBlack();
        }

        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void printRowWhiteView(int row) {
        System.out.printf("%2d ", row);

        for (int col = 1; col <= 8; col++) {
            printSquareWhiteView(row, col);
        }
        System.out.println(RESET_BG_COLOR);
    }

    private void printSquareWhiteView(int row, int col) {
        boolean dark = ((row + col) % 2 == 0);
        System.out.print(dark ? SET_BG_COLOR_MAGENTA : SET_BG_COLOR_LIGHT_GREY);

        String piece = getPieceWhitePerspective(row, col);
        String textColor = getTextColor(piece);

        String cell = piece.isEmpty() ? "   " : " " + piece + " ";
        System.out.print(textColor + cell + RESET_TEXT_COLOR);
    }

    private String getPieceWhitePerspective(int r, int c) {
        if (r == 1) {
            return switch (c) {
                case 1, 8 -> "R";
                case 2, 7 -> "N";
                case 3, 6 -> "B";
                case 4 -> "Q";
                case 5 -> "K";
                default -> "";
            };
        }
        if (r == 2) {
            return "P";
        }
        if (r == 7) {
            return "p";
        }
        if (r == 8) {
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

    private void printColumnsWhite() {
        System.out.print("   ");
        for (char c = 'a'; c <= 'h'; c++) {
            System.out.printf(" %c ", c);
        }
        System.out.println();
    }

    private void printRowBlackView(int row) {
        int label = 9 - row;
        System.out.printf("%2d ", label);

        for (int col = 8; col >= 1; col--) {
            printSquareBlackView(row, col);
        }
        System.out.println(RESET_BG_COLOR);
    }


    private void printSquareBlackView(int row, int col) {
        boolean dark = ((row + col) % 2 == 1);
        System.out.print(dark ? SET_BG_COLOR_MAGENTA : SET_BG_COLOR_LIGHT_GREY);

        String piece = getPieceBlackPerspective(row, col);
        String textColor = getTextColor(piece);
        String cell = piece.isEmpty() ? "   " : " " + piece + " ";

        System.out.print(textColor + cell + RESET_TEXT_COLOR);
    }


    private String getPieceBlackPerspective(int r, int c) {
        if (r == 1) {
            return switch (c) {
                case 1, 8 -> "r";
                case 2, 7 -> "n";
                case 3, 6 -> "b";
                case 4 -> "q";
                case 5 -> "k";
                default -> "";
            };
        }
        if (r == 2) {
            return "p";
        }
        if (r == 7) {
            return "P";
        }
        if (r == 8) {
            return switch (c) {
                case 1, 8 -> "R";
                case 2, 7 -> "N";
                case 3, 6 -> "B";
                case 4 -> "Q";
                case 5 -> "K";
                default -> "";
            };
        }
        return "";
    }

    private void printColumnsBlack() {
        System.out.print("   ");
        for (char c = 'h'; c >= 'a'; c--) {
            System.out.printf(" %c ", c);
        }
        System.out.println();
    }

    private String getTextColor(String piece) {
        if (piece.isEmpty()) {
            return RESET_TEXT_COLOR;
        }
        if (Character.isUpperCase(piece.charAt(0))) {
            return SET_TEXT_COLOR_WHITE;
        } else {
            return SET_TEXT_COLOR_BLACK;
        }
    }
}
