package Pieces;

import java.util.List;

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

    public void setupBoard() {
        for (int i = 0; i < 8; i++) {
            placePiece(new Pawn(Team.WHITE), new Coordinate(i, 1));
            placePiece(new Pawn(Team.BLACK), new Coordinate(i, 6));
        }

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

    public boolean movePiece(Coordinate from, Coordinate to) {
        Piece piece = getPieceAt(from);
        if (piece == null) return false;

        List<Move> availableMoves = piece.availableMoves(this, from);
        for (Move move : availableMoves) {
            if (move.getTo().equals(to)) {
                if (piece instanceof King && Math.abs(from.getX() - to.getX()) == 2) {
                    handleCastling(from, to, piece.getTeam());
                }
                removePiece(from);
                placePiece(piece, to);
                piece.setHasMoved(true);

                if (piece instanceof Pawn) {
                    if ((piece.getTeam() == Team.WHITE && to.getY() == 7) ||
                            (piece.getTeam() == Team.BLACK && to.getY() == 0)) {
                        placePiece(new Queen(piece.getTeam()), to);
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void handleCastling(Coordinate from, Coordinate to, Team team) {
        if (to.getX() == 6) {
            Piece rook = getPieceAt(new Coordinate(7, from.getY()));
            removePiece(new Coordinate(7, from.getY()));
            placePiece(rook, new Coordinate(5, from.getY()));
        } else if (to.getX() == 2) {
            Piece rook = getPieceAt(new Coordinate(0, from.getY()));
            removePiece(new Coordinate(0, from.getY()));
            placePiece(rook, new Coordinate(3, from.getY()));
        }
    }

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

    public boolean isInCheck(Team team) {
        Coordinate kingPos = findKing(team);
        return kingPos != null && isSquareUnderAttack(kingPos, team);
    }

    public boolean isInCheckmate(Team team) {
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

    public Board copy() {
        Board newBoard = new Board();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece original = getPieceAt(new Coordinate(x, y));
                if (original != null) {
                    try {
                        Piece cloned = original.getClass()
                                .getDeclaredConstructor(Team.class)
                                .newInstance(original.getTeam());
                        cloned.setHasMoved(original.hasMoved());
                        newBoard.placePiece(cloned, new Coordinate(x, y));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return newBoard;
    }

    public String[][] generateBoardState() {
        String[][] state = new String[8][8];
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = getPieceAt(new Coordinate(x, y));
                if (piece == null) {
                    state[x][y] = "";
                } else {
                    String code = switch (piece.getType()) {
                        case PAWN -> "P";
                        case ROOK -> "R";
                        case KNIGHT -> "N";
                        case BISHOP -> "B";
                        case QUEEN -> "Q";
                        case KING -> "K";
                    };
                    if (piece.getTeam() == Team.BLACK) {
                        code = code.toLowerCase();
                    }
                    state[x][y] = code;
                }
            }
        }
        return state;
    }

    // New Method: text version of board for terminal display
    public String toTextBoard() {
        StringBuilder sb = new StringBuilder();
        for (int y = 7; y >= 0; y--) {
            sb.append("[SERVER] ");
            for (int x = 0; x < 8; x++) {
                Piece piece = getPieceAt(new Coordinate(x, y));
                if (piece == null) {
                    sb.append(". ");
                } else {
                    char c = piece.getType().toString().charAt(0);
                    if (piece.getTeam() == Team.BLACK) {
                        c = Character.toLowerCase(c);
                    }
                    sb.append(c).append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}


