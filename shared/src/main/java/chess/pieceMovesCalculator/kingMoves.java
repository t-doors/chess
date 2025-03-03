package chess.pieceMovesCalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class kingMoves extends moveCalculator {

    @Override
    public Collection<ChessMove> allPossibleMoves(ChessBoard board, ChessPosition startPos) {
        Collection<ChessMove> kingMoves = new ArrayList<>();
        ChessPiece self = board.getPiece(startPos);
        if (self == null) {
            return kingMoves;
        }

        int[][] kingSteps = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] step : kingSteps) {
            int row = startPos.getRow() + step[0];
            int col = startPos.getColumn() + step[1];
            if (onBoard(row, col)) {
                ChessPosition checkPos = new ChessPosition(row, col);
                if (!occupied(board, checkPos)
                        || canCapture(board, startPos, checkPos)) {
                    kingMoves.add(new ChessMove(startPos, checkPos, null));
                }
            }
        }
        return kingMoves;
    }
}