package chess.PieceMovesCalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class QueenMoves extends MoveCalculator{
    public Collection<ChessMove> allPossibleMoves(ChessBoard board, ChessPosition startPos) {
        Collection<ChessMove> queenMoves = new ArrayList<>();

        int[][] directions = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            queenMoves.addAll(gatherMovesInDirection(board, startPos, dir[0], dir[1], true));
        }
        return queenMoves;
    }
}