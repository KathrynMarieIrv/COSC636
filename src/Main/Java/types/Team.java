// src/main/java/types/Team.java
package Main.Java.types;

public enum Team {
    WHITE,
    BLACK;

    @Override
    public String toString() {
        return this == WHITE ? "White" : "Black";
    }
}
