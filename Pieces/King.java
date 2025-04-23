import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(Team team) {
        super(team, PieceTypes.KING);
    }

    //King can move one square at a time in any direction
    @Override
    public List<Move> availableMoves(Board board, Coordinate currentCoord) {
        List<Move> moves = new ArrayList<>();
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int dir = 0; dir < 8; dir++) {
            int x = currentCoord.getX() + dx[dir];
            int y = currentCoord.getY() + dy[dir];

            if (board.isValidCoordinate(x, y)) {
                Coordinate target = new Coordinate(x, y);
                Piece targetPiece = board.getPieceAt(target);

                if (targetPiece == null || targetPiece.getTeam() != this.getTeam()) {
                    moves.add(new Move(currentCoord, target)); //Take piece
                }
            }
        }

        return moves;
    }
}
