// src/main/types/Team.java
package Main.types;

public enum Team {
    WHITE,
    BLACK;

    @Override
    public String toString() {
        return this == WHITE ? "White" : "Black";
    }
}
