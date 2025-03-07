package chess.piecemovescalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class KnightMoves extends MoveCalculator {
    @Override
    public Collection<ChessMove> allPossibleMoves(ChessBoard board, ChessPosition startPos) {
        Collection<ChessMove> knightMoves = new ArrayList<>();
        ChessPiece self = board.getPiece(startPos);
        if (self == null) {
            return knightMoves;
        }

        int[][] knightJumps = {
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] jump : knightJumps) {
            int newRow = startPos.getRow() + jump[0];
            int newCol = startPos.getColumn() + jump[1];
            if (onBoard(newRow, newCol)) {
                ChessPosition potential = new ChessPosition(newRow, newCol);
                if (!occupied(board, potential) || canCapture(board, startPos, potential)) {
                    knightMoves.add(new ChessMove(startPos, potential, null));
                }
            }
        }
        return knightMoves;
    }
}