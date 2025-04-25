// src/main/Main/board/Board.java
package Main.board;

import java.util.ArrayList;
import java.util.List;
import Main.pieces.*;
import Main.types.*;

public class Board {
    private final Square[][] squares;

    public Board() {
        this.squares = new Square[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                squares[i][j] = new Square(new Coordinate(i, j));
            }
        }
    }

    // Sets up the board with pieces 
    public void setupBoard() {
        // Puts pawns on the board 
        for (int i = 0; i < 8; i++) {
            placePiece(new Pawn(Team.WHITE), new Coordinate(i, 1));
            placePiece(new Pawn(Team.BLACK), new Coordinate(i, 6));
        }

        // Place other pieces on the board
        placePiece(new Rook(Team.WHITE), new Coordinate(0, 0));
        placePiece(new Rook(Team.WHITE), new Coordinate(7, 0));
        placePiece(new Rook(Team.BLACK), new Coordinate(0, 7));
        placePiece(new Rook(Team.BLACK), new Coordinate(7, 7));

        placePiece(new Knight(Team.WHITE), new Coordinate(1, 0));
        placePiece(new Knight(Team.WHITE), new Coordinate(6, 0));
        placePiece(new Knight(Team.BLACK), new Coordinate(1, 7));
        placePiece(new Knight(Team.BLACK), new Coordinate(6, 7));

        placePiece(new Bishop(Team.WHITE), new Coordinate(2, 0));
        placePiece(new Bishop(Team.WHITE), new Coordinate(5, 0));
        placePiece(new Bishop(Team.BLACK), new Coordinate(2, 7));
        placePiece(new Bishop(Team.BLACK), new Coordinate(5, 7));

        placePiece(new Queen(Team.WHITE), new Coordinate(3, 0));
        placePiece(new Queen(Team.BLACK), new Coordinate(3, 7));

        placePiece(new King(Team.WHITE), new Coordinate(4, 0));
        placePiece(new King(Team.BLACK), new Coordinate(4, 7));
    }

    public Square getSquare(Coordinate coord) {
        return squares[coord.getX()][coord.getY()];
    }

    public boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    public Piece getPieceAt(Coordinate coord) {
        return getSquare(coord).getPiece();
    }

    public void placePiece(Piece piece, Coordinate coord) {
        getSquare(coord).placePiece(piece);
    }

    public void removePiece(Coordinate coord) {
        getSquare(coord).removePiece();
    }

    public boolean isSquareUnderAttack(Coordinate square, Team team) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Coordinate from = new Coordinate(x, y);
                Piece piece = getPieceAt(from);

                if (piece != null && piece.getTeam() != team) {
                    List<Move> moves = piece.availableMoves(this, from);
                    for (Move move : moves) {
                        if (move.getTo().equals(square)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // Regular and Special Moves
    public boolean movePiece(Coordinate from, Coordinate to) {
        Piece piece = getPieceAt(from);
        if (piece == null) {
            return false;
        }

        // Castling
        if (isCastlingMove(from, to)) {
            performCastlingMove(from, to);
            return true;
        }

        // Pawn Promotion (handled elsewhere)
        List<Move> availableMoves = piece.availableMoves(this, from);
        for (Move move : availableMoves) {
            if (move.getTo().equals(to)) {
                removePiece(from);
                placePiece(piece, to);
                return true;
            }
        }
        return false;
    }

    // Castling
    private boolean isCastlingMove(Coordinate from, Coordinate to) {
        Piece piece = getPieceAt(from);

        if (!(piece instanceof King king) || king.hasMoved()) return false;

        int deltaX = to.getX() - from.getX();
        int y = from.getY();

        if (Math.abs(deltaX) != 2 || from.getY() != to.getY()) return false;

        boolean isKingside = deltaX > 0;
        Coordinate rookCoord = isKingside ? new Coordinate(7, y) : new Coordinate(0, y);
        Piece rook = getPieceAt(rookCoord);

        if (!(rook instanceof Rook) || rook.hasMoved()) return false;

        // Check squares between king and rook are empty
        int start = Math.min(from.getX(), rookCoord.getX()) + 1;
        int end = Math.max(from.getX(), rookCoord.getX()) - 1;
        for (int x = start; x <= end; x++) {
            if (getPieceAt(new Coordinate(x, y)) != null) return false;
        }
        // Ensure you are not castling into check
        return !(isSquareUnderAttack(from, king.getTeam()) ||
                 isSquareUnderAttack(new Coordinate(from.getX() + (isKingside ? 1 : -1), y), king.getTeam()) ||
                 isSquareUnderAttack(to, king.getTeam()));
    }

    private void performCastlingMove(Coordinate from, Coordinate to) {
        Piece king = getPieceAt(from);
        int y = from.getY();
        boolean isKingside = to.getX() > from.getX();

        Coordinate rookFrom = isKingside ? new Coordinate(7, y) : new Coordinate(0, y);
        Coordinate rookTo   = isKingside ? new Coordinate(5, y) : new Coordinate(3, y);

        Piece rook = getPieceAt(rookFrom);

        // Move King
        removePiece(from);
        placePiece(king, to);

        // Move Rook
        removePiece(rookFrom);
        placePiece(rook, rookTo);
    }

    // Finding the King
    public Coordinate findKing(Team team) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = getPieceAt(new Coordinate(x, y));
                if (piece instanceof King && piece.getTeam() == team) {
                    return new Coordinate(x, y);
                }
            }
        }
        return null;
    }

    // Checking if King is in Check
    public boolean isInCheck(Team team) {
        Coordinate kingPos = findKing(team);
        return isSquareUnderAttack(kingPos, team);
    }

    /**
     * Check for stalemate: the given team is NOT in check, 
     * but has no legal moves that would escape check.
     */
    public boolean isStalemate(Team team) {
        // If in check, it’s not stalemate
        if (isInCheck(team)) return false;

        // For every piece of that team, see if any move avoids check
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Coordinate from = new Coordinate(x, y);
                Piece piece = getPieceAt(from);
                if (piece != null && piece.getTeam() == team) {
                    List<Move> moves = piece.availableMoves(this, from);
                    for (Move move : moves) {
                        Board copy = this.copy();
                        // Use movePiece on copy to test
                        if (copy.movePiece(from, move.getTo())) {
                            // If after that move the king is not in check, it's not stalemate
                            if (!copy.isInCheck(team)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        // No legal escape moves found → stalemate
        return true;
    }

    
    // Checking if King is in Checkmate
    public boolean isCheckmate(Team team) {
        if (!isInCheck(team)) return false;

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Coordinate from = new Coordinate(x, y);
                Piece piece = getPieceAt(from);
                if (piece != null && piece.getTeam() == team) {
                    List<Move> moves = piece.availableMoves(this, from);
                    for (Move move : moves) {
                        Board copy = this.copy();
                        copy.movePiece(from, move.getTo());
                        if (!copy.isInCheck(team)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Create a shallow copy of board for move checks.
     */
    public Board copy() {
        Board clone = new Board();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece p = this.getPieceAt(new Coordinate(x, y));
                if (p != null) {
                    // reuse the same piece reference
                    clone.placePiece(p, new Coordinate(x, y));
                }
            }
        }
        return clone;
    }

    /**
     * Given a promotion code ("Q", "R", "B", or "N"), return
     * a fresh Piece instance of the correct type for that team.
     */
    public Piece makePromotionPiece(String code, Team team) {
        switch (code.toUpperCase()) {
            case "R": return new Rook(team);
            case "B": return new Bishop(team);
            case "N": return new Knight(team);
            case "Q":
            default:  return new Queen(team);
        }
    }

    public void displayBoard() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = getPieceAt(new Coordinate(i, j));
                if (piece == null) {
                    System.out.print("[ ] ");
                } else {
                    System.out.print("[" + piece.getType().toString().substring(0, 1) + "] ");
                }
            }
            System.out.println();
        }
    }

    public List<String> renderLines() {
        List<String> rows = new ArrayList<>();
        // y = 7 down to 0 gives rank 8 → rank 1
        for (int y = 7; y >= 0; y--) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < 8; x++) {
                Piece p = getPieceAt(new Coordinate(x, y));
                if (p == null) {
                    sb.append("[ ] ");
                } else {
                    char symbol = p.getType().toString().charAt(0);
                    // lowercase if Black
                    if (p.getTeam() == Team.BLACK) {
                        symbol = Character.toLowerCase(symbol);
                    }
                    sb.append("[").append(symbol).append("] ");
                }
            }
            rows.add(sb.toString());
        }
        return rows;
    }
    
}
