// src/main/pieces/Pawn.java
package Main.pieces;

import java.util.ArrayList;
import java.util.List;
import Main.types.*;
import Main.board.*;

public class Pawn extends Piece {

    public Pawn(Team team) {
        super(team, PieceTypes.PAWN);
    }

    @Override
    public List<Move> availableMoves(Board board, Coordinate currentCoord) {
        List<Move> moves = new ArrayList<>();
        int direction = (this.getTeam() == Team.WHITE) ? 1 : -1;
        int startRow = (this.getTeam() == Team.WHITE) ? 1 : 6;

        // Pawns move one square forward at a time
        if (board.isValidCoordinate(currentCoord.getX(), currentCoord.getY() + direction) &&
            board.getPieceAt(new Coordinate(currentCoord.getX(), currentCoord.getY() + direction)) == null) {
            moves.add(new Move(currentCoord, new Coordinate(currentCoord.getX(), currentCoord.getY() + direction)));
        }

        // Pawns can move two squares forward if its their first move
        if (currentCoord.getY() == startRow &&
            board.getPieceAt(new Coordinate(currentCoord.getX(), currentCoord.getY() + direction)) == null &&
            board.getPieceAt(new Coordinate(currentCoord.getX(), currentCoord.getY() + 2 * direction)) == null) {
            moves.add(new Move(currentCoord, new Coordinate(currentCoord.getX(), currentCoord.getY() + 2 * direction)));
        }

        // Pawns take other pieces diagonally
        for (int dx = -1; dx <= 1; dx += 2) {
            int x = currentCoord.getX() + dx;
            int y = currentCoord.getY() + direction;

            if (board.isValidCoordinate(x, y)) {
                Coordinate target = new Coordinate(x, y);
                Piece targetPiece = board.getPieceAt(target);

                if (targetPiece != null && targetPiece.getTeam() != this.getTeam()) {
                    moves.add(new Move(currentCoord, target)); 
                }
            }
        }

        return moves;
    }
}
