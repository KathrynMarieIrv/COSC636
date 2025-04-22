import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

    public Bishop(Team team) {
        super(team, PieceTypes.BISHOP);
    }

    //Bishop moves diagonally any number of unblocked squares 
    @Override
    public List<Move> availableMoves(Board board, Coordinate currentCoord) {
        List<Move> moves = new ArrayList<>();

        int[] dx = {1, 1, -1, -1};
        int[] dy = {1, -1, 1, -1};

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
                        moves.add(new Move(currentCoord, target)); // Take Piece
                    }
                    break; // Blocked by a piece
                }
            }
        }

        return moves;
    }
}
