public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;

        player1.setOpponent(player2);
        player2.setOpponent(player1);

        if (Math.random() < 0.5) {
            player1.setColor("WHITE");
            player2.setColor("BLACK");
        } else {
            player1.setColor("BLACK");
            player2.setColor("WHITE");
        }

        player1.startGame();
        player2.startGame();
    }

    public void endSession() {
        player1.endGame();
        player2.endGame();
    }
}
