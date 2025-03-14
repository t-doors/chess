package chess.piecemovescalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;


public class BishopMoves extends MovesCalculator {

    @Override
    public Collection<ChessMove> allPossibleMoves(ChessBoard board, ChessPosition startPos) {
        Collection<ChessMove> bishopMoves = new ArrayList<>();

        int[][] diagonals = {
                {1, 1},   // up-right
                {1, -1},  // up-left
                {-1, 1},  // down-right
                {-1, -1}  // down-left
        };

        for (int[] diag : diagonals) {
            bishopMoves.addAll(gatherMovesInDirection(board, startPos, diag[0], diag[1], true));
        }
        return bishopMoves;
    }
}
