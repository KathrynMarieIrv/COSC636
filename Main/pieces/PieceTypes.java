// src/main/pieces/PieceTypes.java
package Main.pieces;

public enum PieceTypes {
    PAWN,
    KNIGHT,
    BISHOP,
    ROOK,
    QUEEN,
    KING;

    @Override
    public String toString() {
        return switch (this) {
            case PAWN -> "Pawn";
            case KNIGHT -> "Knight";
            case BISHOP -> "Bishop";
            case ROOK -> "Rook";
            case QUEEN -> "Queen";
            case KING -> "King";
        };
    }
}
