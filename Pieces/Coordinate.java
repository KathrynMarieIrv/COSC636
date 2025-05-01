package Pieces;

public class Coordinate {
    private final int x;
    private final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Coordinate)) return false;
        Coordinate other = (Coordinate) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    // ✨ NEW: Convert standard chess notation like "e2" -> Coordinate(x,y)
    public static Coordinate fromAlgebraic(String text) {
        if (text.length() != 2) {
            throw new IllegalArgumentException("Invalid coordinate: " + text);
        }
        char file = text.toLowerCase().charAt(0); // column letter (a-h)
        char rank = text.charAt(1);               // row number (1-8)

        int x = file - 'a';       // 'a' -> 0, 'b' -> 1, ..., 'h' -> 7
        int y = rank - '1';       // '1' -> 0, '2' -> 1, ..., '8' -> 7

        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Invalid coordinate: " + text);
        }
        return new Coordinate(x, y);
    }
}

