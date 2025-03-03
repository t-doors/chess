package chess.pieceMovesCalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class moveCalculator {

    public Collection<ChessMove> allPossibleMoves(ChessBoard board, ChessPosition startPos) {
        return new ArrayList<>();
    }

    protected boolean onBoard(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }


    protected boolean occupied(ChessBoard board, ChessPosition toPos) {
        return (board.getPiece(toPos) != null);
    }


    protected boolean canCapture(ChessBoard board, ChessPosition fromPos, ChessPosition toPos) {
        ChessPiece fromPiece = board.getPiece(fromPos);
        ChessPiece toPiece = board.getPiece(toPos);

        if (fromPiece == null || toPiece == null) {
            return false;
        }
        return fromPiece.getTeamColor() != toPiece.getTeamColor();
    }

    protected Collection<ChessMove> gatherMovesInDirection(
            ChessBoard board,
            ChessPosition origin,
            int dRow,
            int dCol,
            boolean keepGoing
    ) {
        Collection<ChessMove> movesFound = new ArrayList<>();

        int row = origin.getRow();
        int col = origin.getColumn();

        while (true) {
            row += dRow;
            col += dCol;

            if (!onBoard(row, col)) {
                break;
            }
            ChessPosition nextPos = new ChessPosition(row, col);
            ChessMove nextMove = new ChessMove(origin, nextPos, null);

            if (occupied(board, nextPos)) {
                if (canCapture(board, origin, nextPos)) {
                    movesFound.add(nextMove);
                }
                break;
            } else {
                movesFound.add(nextMove);
                if (!keepGoing) {
                    break;
                }
            }
        }

        return movesFound;
    }
}
