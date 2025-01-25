package chess.PieceMovesCalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class PawnMoves extends MoveCalculator{
    @Override
    public Collection<ChessMove> allPossibleMoves(ChessBoard board, ChessPosition startPos) {
        Collection<ChessMove> pawnMoves = new ArrayList<>();
        ChessPiece self = board.getPiece(startPos);
        if (self == null) {
            return pawnMoves;
        }

        int direction = (self.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int row = startPos.getRow();
        int col = startPos.getColumn();

        int forwardRow = row + direction;
        if (onBoard(forwardRow, col)) {
            ChessPosition oneAhead = new ChessPosition(forwardRow, col);
            if (!occupied(board, oneAhead)) {
                pawnMoves.add(new ChessMove(startPos, oneAhead, null));

                boolean onStartLine = (self.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2)
                        || (self.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7);

                if (onStartLine) {
                    int twoRow = row + (2 * direction);
                    ChessPosition twoAhead = new ChessPosition(twoRow, col);
                    if (onBoard(twoRow, col) && !occupied(board, twoAhead)) {
                        pawnMoves.add(new ChessMove(startPos, twoAhead, null));
                    }
                }
            }
        }

        int[][] diagonals = {
                {direction, -1},
                {direction, 1}
        };
        for (int[] diag : diagonals) {
            int newR = row + diag[0];
            int newC = col + diag[1];
            if (onBoard(newR, newC)) {
                ChessPosition diagPos = new ChessPosition(newR, newC);
                if (canCapture(board, startPos, diagPos)) {
                    pawnMoves.add(new ChessMove(startPos, diagPos, null));
                }
            }
        }
        return pawnMoves;
    }
}