// src/main/pieces/Piece.java
package Main.pieces;

import java.util.List;
import Main.board.Board;
import Main.types.*;

public abstract class Piece {
    private Team team;
    private PieceTypes type;
    private boolean taken;
    private boolean hasMoved = false;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public Piece(Team team, PieceTypes type) {
        this.setTeam(team);
        this.setType(type);
    }

    public PieceTypes getType() {
        return type;
    }

    public void setType(PieceTypes type) {
        this.type = type;
    }

    public Team getTeam() {
        return this.team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean moved) {
        this.hasMoved = moved;
    }

    public boolean isTaken() {
        return this.taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    @Override
    public String toString() {
        return this.team.toString() + " " + this.type.toString();
    }

    public abstract List<Move> availableMoves(Board board, Coordinate currentCoord);
}
