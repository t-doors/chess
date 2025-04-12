package client;

import static ui.EscapeSequences.*;

public class Board {

    public void drawChessBoard(boolean blackView) {
        System.out.print(ERASE_SCREEN);

        if (!blackView) {
            for (int row = 8; row >= 1; row--) {
                printRow(row, row, 1, 8, 1, true);
            }
            printColumns(true);
        } else {
            for (int row = 8; row >= 1; row--) {
                printRow(row, 9 - row, 8, 1, -1, false);
            }
            printColumns(false);
        }

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

        String piece;
        if (whiteView) {
            piece = getPieceWhitePerspective(row, col);
        } else {
            piece = getPieceBlackPerspective(row, col);
        }
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

    private String getTextColor(String piece) {
        if (piece.isEmpty()) {
            return RESET_TEXT_COLOR;
        }
        return (Character.isUpperCase(piece.charAt(0)))
                ? SET_TEXT_COLOR_WHITE
                : SET_TEXT_COLOR_BLACK;
    }
}
