// src/main/java/pieces/Knight.java
package Main.Java.pieces;

import java.util.ArrayList;
import java.util.List;
import Main.Java.types.*;
import Main.Java.board.*;

public class Knight extends Piece {

    public Knight(Team team) {
        super(team, PieceTypes.KNIGHT);
    }

    //Knight can jump over other pieces, moves up 2, over 1 in any direction
    @Override
    public List<Move> availableMoves(Board board, Coordinate currentCoord) {
        List<Move> moves = new ArrayList<>();
        int[][] directions = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1}, 
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };

        for (int[] dir : directions) {
            int x = currentCoord.getX() + dir[0];
            int y = currentCoord.getY() + dir[1];

            if (board.isValidCoordinate(x, y)) {
                Coordinate target = new Coordinate(x, y);
                Piece targetPiece = board.getPieceAt(target);

                if (targetPiece == null || targetPiece.getTeam() != this.getTeam()) {
                    moves.add(new Move(currentCoord, target)); //Take Piece
                }
            }
        }

        return moves;
    }
}
