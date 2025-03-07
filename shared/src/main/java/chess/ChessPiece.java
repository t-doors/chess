package chess;

import chess.piecemovescalculator.*;

import java.util.Objects;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor teamColor;
    private final PieceType pieceType;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;

    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (pieceType) {
            case BISHOP -> new BishopMoves().allPossibleMoves(board, myPosition);
            case KING -> new KingMoves().allPossibleMoves(board, myPosition);
            case QUEEN -> new QueenMoves().allPossibleMoves(board, myPosition);
            case KNIGHT -> new KnightMoves().allPossibleMoves(board, myPosition);
            case PAWN -> new PawnMoves().allPossibleMoves(board, myPosition);
            case ROOK -> new RookMoves().allPossibleMoves(board, myPosition);
            default -> new MovesCalculator().allPossibleMoves(board, myPosition);
        };
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return teamColor == that.teamColor && pieceType == that.pieceType;
    }
    @Override
    public int hashCode() {
        return Objects.hash(teamColor, pieceType);
    }
}

