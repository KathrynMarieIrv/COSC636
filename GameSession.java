import java.time.Duration;
import java.time.Instant;
import javax.swing.SwingUtilities;
import Pieces.Board;
import Pieces.Coordinate;
import Pieces.Team;

public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final Board board;
    private ClientHandler currentPlayer;
    private ClientHandler waitingPlayer;
    private Instant turnStartTime;
    private Duration player1Time;
    private Duration player2Time;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.board = new Board();
        board.setupBoard();

        this.currentPlayer = player1;
        this.waitingPlayer = player2;
        this.player1.setColor("WHITE");
        this.player2.setColor("BLACK");
        this.turnStartTime = Instant.now();
        this.player1Time = Duration.ZERO;
        this.player2Time = Duration.ZERO;

        player1.setOpponent(player2);
        player2.setOpponent(player1);

        player1.startGame();
        player2.startGame();

        printBoardState();
        sendBoardToClients();
    }

    public synchronized void handleMove(ClientHandler player, String moveCommand) {
        if (player != currentPlayer) {
            player.sendMessage("Not your turn.");
            return;
        }

        String[] parts = moveCommand.split("\\s+");
        if (parts.length != 2) {
            player.sendMessage("Invalid move format. Use: MOVE e2 e4");
            return;
        }

        Coordinate from = parseCoordinate(parts[0]);
        Coordinate to = parseCoordinate(parts[1]);

        if (from == null || to == null) {
            player.sendMessage("Invalid coordinates.");
            return;
        }

        boolean success = board.movePiece(from, to);

        if (success) {
            updateTimers();
            printBoardState();
            sendBoardToClients();
            checkGameState();
            switchTurns();
        } else {
            player.sendMessage("Invalid move.");
        }
    }

    private void switchTurns() {
        ClientHandler temp = currentPlayer;
        currentPlayer = waitingPlayer;
        waitingPlayer = temp;
        turnStartTime = Instant.now();
    }

    private void updateTimers() {
        Instant now = Instant.now();
        Duration elapsed = Duration.between(turnStartTime, now);

        if (currentPlayer == player1) {
            player1Time = player1Time.plus(elapsed);
        } else {
            player2Time = player2Time.plus(elapsed);
        }
    }

    private Coordinate parseCoordinate(String input) {
        if (input.length() != 2) return null;

        char file = Character.toLowerCase(input.charAt(0));
        char rank = input.charAt(1);

        int x = file - 'a';
        int y = Character.getNumericValue(rank) - 1;

        if (x < 0 || x > 7 || y < 0 || y > 7) return null;
        return new Coordinate(x, y);
    }

    private String formatTime(Duration duration) {
        long seconds = duration.getSeconds();
        long min = seconds / 60;
        long sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    private void printBoardState() {
        String[][] state = board.generateBoardState();

        System.out.println();
        System.out.println("[SERVER] BLACK (" + formatTime(player2Time) + ")");
        System.out.println("[SERVER]    A B C D E F G H");
        System.out.println("[SERVER]  +-----------------+");
        for (int y = 7; y >= 0; y--) {
            System.out.print("[SERVER] " + (y + 1) + " |");
            for (int x = 0; x < 8; x++) {
                String piece = state[x][y];
                System.out.print(" " + (piece.isEmpty() ? "." : piece));
            }
            System.out.println(" |");
        }
        System.out.println("[SERVER]  +-----------------+");
        System.out.println("[SERVER]    A B C D E F G H");
        System.out.println("[SERVER] WHITE (" + formatTime(player1Time) + ")");
        System.out.println("[SERVER] ================================");
    }

    private void sendBoardToClients() {
        StringBuilder sb = new StringBuilder();
        String[][] state = board.generateBoardState();

        sb.append("BLACK (").append(formatTime(player2Time)).append(")\n");
        sb.append("   A B C D E F G H\n");
        sb.append(" +-----------------+\n");
        for (int y = 7; y >= 0; y--) {
            sb.append(" ").append(y + 1).append(" |");
            for (int x = 0; x < 8; x++) {
                String piece = state[x][y];
                sb.append(" ").append(piece.isEmpty() ? "." : piece);
            }
            sb.append(" |\n");
        }
        sb.append(" +-----------------+\n");
        sb.append("   A B C D E F G H\n");
        sb.append("WHITE (").append(formatTime(player1Time)).append(")\n");
        sb.append("=================================\n");

        player1.sendMessage(sb.toString());
        player2.sendMessage(sb.toString());
    }

    private void checkGameState() {
        Team team = currentPlayer.getColor().equals("WHITE") ? Team.WHITE : Team.BLACK;

        if (board.isInCheckmate(team)) {
            currentPlayer.sendMessage("You are checkmated! You lose.");
            waitingPlayer.sendMessage("You delivered checkmate! You win!");
            player1.endGame();
            player2.endGame();
        } else if (board.isInCheck(team)) {
            currentPlayer.sendMessage("You are in check!");
        }
    }

    public void endSession() {
        player1.sendMessage("Opponent disconnected. Game over!");
        player2.sendMessage("Opponent disconnected. Game over!");
        player1.endGame();
        player2.endGame();
    }
}

