package chess.pieceMovesCalculator;

import chess.*;

import java.util.ArrayList;
import java.util.Collection;

public class pawnMoves extends moveCalculator {
    @Override
    public Collection<ChessMove> allPossibleMoves(ChessBoard board, ChessPosition startPos) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece pawn = board.getPiece(startPos);

        if (pawn == null) {
            return moves;
        }

        boolean isWhite = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE);
        int step = isWhite ? 1 : -1;

        int row = startPos.getRow();
        int col = startPos.getColumn();

        int oneForwardRow = row + step;
        if (onBoard(oneForwardRow, col)) {
            ChessPosition oneForward = new ChessPosition(oneForwardRow, col);
            if (!occupied(board, oneForward)) {
                if (isPromotionRank(oneForwardRow, isWhite)) {
                    moves.addAll(createPromotions(startPos, oneForward));
                } else {
                    moves.add(new ChessMove(startPos, oneForward, null));
                }

                if ((isWhite && row == 2) || (!isWhite && row == 7)) {
                    int twoForwardRow = row + (2 * step);
                    if (onBoard(twoForwardRow, col)) {
                        ChessPosition twoForward = new ChessPosition(twoForwardRow, col);
                        if (!occupied(board, twoForward)) {
                            moves.add(new ChessMove(startPos, twoForward, null));
                        }
                    }
                }
            }
        }

        int[][] diagonalOffsets = {
                {step, -1},
                {step, 1}
        };

        for (int[] offset : diagonalOffsets) {
            int newRow = row + offset[0];
            int newCol = col + offset[1];
            if (onBoard(newRow, newCol)) {
                ChessPosition diagPos = new ChessPosition(newRow, newCol);
                if (canCapture(board, startPos, diagPos)) {
                    if (isPromotionRank(newRow, isWhite)) {
                        moves.addAll(createPromotions(startPos, diagPos));
                    } else {
                        moves.add(new ChessMove(startPos, diagPos, null));
                    }
                }
            }
        }
        return moves;
    }


    private boolean isPromotionRank(int row, boolean isWhitePawn) {
        return (isWhitePawn && row == 8) || (!isWhitePawn && row == 1);
    }

    private Collection<ChessMove> createPromotions(ChessPosition fromPos, ChessPosition toPos) {
        Collection<ChessMove> promos = new ArrayList<>();
        promos.add(new ChessMove(fromPos, toPos, ChessPiece.PieceType.QUEEN));
        promos.add(new ChessMove(fromPos, toPos, ChessPiece.PieceType.BISHOP));
        promos.add(new ChessMove(fromPos, toPos, ChessPiece.PieceType.KNIGHT));
        promos.add(new ChessMove(fromPos, toPos, ChessPiece.PieceType.ROOK));
        return promos;
    }
}