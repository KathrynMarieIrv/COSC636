package Pieces;

public class Move {
    private final Coordinate from;
    private final Coordinate to;

    public Move(Coordinate from, Coordinate to) {
        this.from = from;
        this.to = to;
    }

    public Coordinate getFrom() {
        return from;
    }

    public Coordinate getTo() {
        return to;
    }

    @Override
    public String toString() {
        return "Move from " + from + " to " + to;
    }

    // Add this method to parse "e2 e4" style input
    public static Move fromText(String moveText) {
        String[] parts = moveText.trim().split("\\s+");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid move format. Expected: <from> <to>");
        }
        Coordinate from = Coordinate.fromAlgebraic(parts[0]);
        Coordinate to = Coordinate.fromAlgebraic(parts[1]);
        return new Move(from, to);
    }
}

