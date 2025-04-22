public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final Board board;
    private Team currentTurn;

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.board = new Board();
        this.board.setupBoard();
        this.currentTurn = Team.WHITE;

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

    public boolean handleMove(Coordinate from, Coordinate to) {
        Piece piece = board.getPieceAt(from);
        if (piece == null || piece.getTeam() != currentTurn) {
            return false;
        }
    
        boolean moveSuccess = board.movePiece(from, to);
        if (!moveSuccess) {
            return false;
        }
    
        // Check for check/checkmate after the move
        Team opponent = (currentTurn == Team.WHITE) ? Team.BLACK : Team.WHITE;
    
        if (board.isCheckmate(opponent)) {
            sendToBoth("Checkmate! " + currentTurn + " wins.");
            endSession();
        } else if (board.isInCheck(opponent)) {
            sendToBoth("Check!");
        }
    
        // Switch turn
        currentTurn = opponent;
        return true;
    }

    private void sendToBoth(String message) {
        player1.sendMessage(message);
        player2.sendMessage(message);
    }
    

    public void endSession() {
        player1.endGame();
        player2.endGame();
    }
}
