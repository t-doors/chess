package chess;


import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTurn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> rawMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : rawMoves) {
            if (!causesOwnKingCheck(move)) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece startPiece = board.getPiece(move.getStartPosition());

        if (startPiece == null) {
            throw new InvalidMoveException("No piece at starting position.");
        }

        if (startPiece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("It is not " + startPiece.getTeamColor() + "'s turn.");
        }

        Collection<ChessMove> possibleMoves = validMoves(move.getStartPosition());

        if (possibleMoves == null || !possibleMoves.contains(move)) {
            throw new InvalidMoveException("Move " + move + " is not valid for this piece.");
        }
        executeBoardMove(move);

        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private ChessBoard copyBoard(ChessBoard original) {
        ChessBoard clone = new ChessBoard();
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = original.getPiece(pos);
                if (piece != null) {
                    ChessPiece copyPiece =
                            new ChessPiece(piece.getTeamColor(), piece.getPieceType());
                    clone.addPiece(pos, copyPiece);
                }
            }
        }
        return clone;
    }


    private ChessPosition findKingPosition(TeamColor color, ChessBoard theBoard) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition pos = new ChessPosition(r, c);
                ChessPiece piece = theBoard.getPiece(pos);
                if (piece != null
                        && piece.getTeamColor() == color
                        && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return pos;
                }
            }
        }
        return null;
    }


    private boolean underAttack(ChessPosition kingPos, TeamColor kingColor, ChessBoard theBoard) {
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPosition enemyPos = new ChessPosition(r, c);
                ChessPiece enemyPiece = theBoard.getPiece(enemyPos);
                if (enemyPiece != null && enemyPiece.getTeamColor() != kingColor) {
                    Collection<ChessMove> enemyMoves = enemyPiece.pieceMoves(theBoard, enemyPos);
                    for (ChessMove mov : enemyMoves) {
                        if (mov.getEndPosition().equals(kingPos)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    private boolean isInCheckOnBoard(TeamColor color, ChessBoard tempBoard) {
        ChessPosition kingSpot = findKingPosition(color, tempBoard);
        if (kingSpot == null) {
            return false;
        }
        return underAttack(kingSpot, color, tempBoard);
    }


    private boolean causesOwnKingCheck(ChessMove move) {
        ChessPiece mover = board.getPiece(move.getStartPosition());
        if (mover == null) {
            return false;
        }
        TeamColor moverColor = mover.getTeamColor();

        ChessBoard temp = copyBoard(board);

        ChessPiece tempPiece = temp.getPiece(move.getStartPosition());
        temp.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            tempPiece = new ChessPiece(moverColor, move.getPromotionPiece());
        }
        temp.addPiece(move.getEndPosition(), tempPiece);

        return isInCheckOnBoard(moverColor, temp);
    }

    private void executeBoardMove(ChessMove move) {
        ChessPiece movingPiece = board.getPiece(move.getStartPosition());
        board.addPiece(move.getStartPosition(), null);

        if (move.getPromotionPiece() != null) {
            movingPiece = new ChessPiece(movingPiece.getTeamColor(), move.getPromotionPiece());
        }
        board.addPiece(move.getEndPosition(), movingPiece);
    }






}


