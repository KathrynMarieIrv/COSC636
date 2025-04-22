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

    Object getTarget() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
