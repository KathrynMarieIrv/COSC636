package Pieces;

public class Square {
    private Coordinate coordinate;
    private Piece piece;  // Piece occupying the square (null if empty)

    public Square(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.piece = null; // Initially, the square is empty
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public boolean isOccupied() {
        return this.piece != null;
    }

    public void removePiece() {
        this.piece = null;
    }

    public void placePiece(Piece piece) {
        this.piece = piece;
    }
}

