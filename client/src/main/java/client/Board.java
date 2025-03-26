package client;

import static ui.EscapeSequences.*;

public class Board {
    public void drawChessBoard(boolean blackView) {
        System.out.print(ERASE_SCREEN);

        if (blackView) {
            for (int row = 1; row <= 8; row++) {
                printRow(row, true);
            }
            System.out.println("   h  g  f  e  d  c  b  a");
        } else {
            for (int row = 8; row >= 1; row--) {
                printRow(row, false);
            }
            System.out.println("   a  b  c  d  e  f  g  h");
        }

        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void printRow(int row, boolean blackView) {
        System.out.printf("%2d ", row);
        if (blackView) {
            for (int col = 8; col >= 1; col--) {
                printSquare(row, col,true);
            }
        } else {
            for (int col = 1; col <= 8; col++) {
                printSquare(row, col, false);
            }
        }
        System.out.println(RESET_BG_COLOR);
    }

    private void printSquare(int row, int col, boolean blackView) {
        boolean dark = ((row + col) % 2 == 0);
        if (dark) {
            System.out.print(SET_BG_COLOR_BLUE);
        } else {
            System.out.print(SET_BG_COLOR_LIGHT_GREY);
        }

        int chessRow = blackView ? (9 - row) : row;
        int chessColumn = blackView ? (9 - col) : col;

        String piece = getPiece(chessRow, chessColumn);
        String textColor = getTextColor(chessRow, piece);

        System.out.print(textColor + piece + RESET_TEXT_COLOR);
    }

    private String getPiece(int chessRow, int chessColumn) {
        if (chessRow == 1 || chessRow == 8) {
            return switch (chessColumn) {
                case 1, 8 -> chessRow == 1 ? BLACK_ROOK : WHITE_ROOK;
                case 2, 7 -> chessRow == 1 ? BLACK_KNIGHT : WHITE_KNIGHT;
                case 3, 6 -> chessRow == 1 ? BLACK_BISHOP : WHITE_BISHOP;
                case 4 -> chessRow == 1 ? BLACK_QUEEN : WHITE_QUEEN;
                case 5 -> chessRow == 1 ? BLACK_KING : WHITE_KING;
                default -> EMPTY;
            };
        } else if (chessRow == 2) {
            return BLACK_PAWN;
        } else if (chessRow == 7) {
            return WHITE_PAWN;
        } else {
            return EMPTY;
        }
    }

    private String getTextColor(int chessRow, String piece) {
        if (piece.equals(EMPTY)) {
            return RESET_TEXT_COLOR;
        }
        return piece.contains("WHITE") ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
    }
}