package Pieces;

public enum Team {
    WHITE,
    BLACK;

    @Override
    public String toString() {
        return this == WHITE ? "White" : "Black";
    }
}
