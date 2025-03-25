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
                printSquare(row, col);
            }
        } else {
            for (int col = 1; col <= 8; col++) {
                printSquare(row, col);
            }
        }
        System.out.print(RESET_BG_COLOR);
        System.out.println();
    }


    private void printSquare(int row, int col) {
        boolean isDarkSquare = ((row + col) % 2 == 0);

        if (isDarkSquare) {
            System.out.print(SET_BG_COLOR_BLUE);
        } else {
            System.out.print(SET_BG_COLOR_LIGHT_GREY);
        }

        System.out.print("   ");
    }
}
