package Pieces;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {

    public Queen(Team team) {
        super(team, PieceTypes.QUEEN);
    }

    @Override
    public List<Move> availableMoves(Board board, Coordinate currentCoord) {
        List<Move> moves = new ArrayList<>();

        // Queen moves like Rook (straight lines)
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        for (int dir = 0; dir < 4; dir++) {
            int x = currentCoord.getX();
            int y = currentCoord.getY();

            while (true) {
                x += dx[dir];
                y += dy[dir];

                if (!board.isValidCoordinate(x, y)) {
                    break;
                }

                Coordinate target = new Coordinate(x, y);
                Piece targetPiece = board.getPieceAt(target);

                if (targetPiece == null) {
                    moves.add(new Move(currentCoord, target));
                } else {
                    if (targetPiece.getTeam() != this.getTeam()) {
                        moves.add(new Move(currentCoord, target));
                    }
                    break;
                }
            }
        }

        // Queen moves like Bishop (diagonals)
        int[] dx_diag = {1, 1, -1, -1};
        int[] dy_diag = {1, -1, 1, -1};

        for (int dir = 0; dir < 4; dir++) {
            int x = currentCoord.getX();
            int y = currentCoord.getY();

            while (true) {
                x += dx_diag[dir];
                y += dy_diag[dir];

                if (!board.isValidCoordinate(x, y)) {
                    break;
                }

                Coordinate target = new Coordinate(x, y);
                Piece targetPiece = board.getPieceAt(target);

                if (targetPiece == null) {
                    moves.add(new Move(currentCoord, target));
                } else {
                    if (targetPiece.getTeam() != this.getTeam()) {
                        moves.add(new Move(currentCoord, target));
                    }
                    break;
                }
            }
        }

        return moves;
    }
}

