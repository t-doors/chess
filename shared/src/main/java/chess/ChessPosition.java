package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int column;

    public ChessPosition(int row, int col) {
        if (row < 1 || row > 8 || col < 1 || col > 8) {
            throw new IllegalArgumentException(
                    "Row and column must each be between 1 and 8. Provided: (" + row + ", " + col + ")");
        }
        this.row = row;
        this.column = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + column + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof ChessPosition other) {
            return this.row == other.row && this.column == other.column;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }
}
