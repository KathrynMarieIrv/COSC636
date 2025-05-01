package Pieces;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    public Pawn(Team team) {
        super(team, PieceTypes.PAWN);
    }

    @Override
    public List<Move> availableMoves(Board board, Coordinate currentCoord) {
        List<Move> moves = new ArrayList<>();
        int direction = (this.getTeam() == Team.WHITE) ? 1 : -1;
        int startRow = (this.getTeam() == Team.WHITE) ? 1 : 6;

        // Move one square forward
        if (board.isValidCoordinate(currentCoord.getX(), currentCoord.getY() + direction) &&
                board.getPieceAt(new Coordinate(currentCoord.getX(), currentCoord.getY() + direction)) == null) {
            moves.add(new Move(currentCoord, new Coordinate(currentCoord.getX(), currentCoord.getY() + direction)));
        }

        // Move two squares forward from starting position
        if (currentCoord.getY() == startRow &&
                board.getPieceAt(new Coordinate(currentCoord.getX(), currentCoord.getY() + direction)) == null &&
                board.getPieceAt(new Coordinate(currentCoord.getX(), currentCoord.getY() + 2 * direction)) == null) {
            moves.add(new Move(currentCoord, new Coordinate(currentCoord.getX(), currentCoord.getY() + 2 * direction)));
        }

        // Capture diagonally
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
