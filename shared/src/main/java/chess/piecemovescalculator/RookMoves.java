package chess.piecemovescalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class RookMoves extends MoveCalculator {
    @Override
    public Collection<ChessMove> allPossibleMoves(ChessBoard board, ChessPosition startPos) {
        Collection<ChessMove> rookMoves = new ArrayList<>();

        int[][] lines = {
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}

        };

        for (int[] line : lines) {
            rookMoves.addAll(gatherMovesInDirection(board, startPos, line[0], line[1], true));
        }
        return rookMoves;
    }
}