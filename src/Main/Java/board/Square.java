// src/main/java/board/Square.java
package Main.Java.board;

import Main.Java.pieces.*;
import Main.Java.types.*;

public class Square {
    private Coordinate coordinate;
    private Piece piece;  // This will be the piece occupying the square (null if empty)

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
