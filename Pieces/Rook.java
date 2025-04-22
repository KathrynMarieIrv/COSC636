import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    public Rook(Team team) {
        super(team, PieceTypes.ROOK);
    }

    //Rook moves Forward, Back, Left, Right any number of unblocked squares
    @Override
    public List<Move> availableMoves(Board board, Coordinate currentCoord) {
        List<Move> moves = new ArrayList<>();

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
                        moves.add(new Move(currentCoord, target)); // Take Piece
                    }
                    break; // Blocked
                }
            }
        }

        return moves;
    }
}
