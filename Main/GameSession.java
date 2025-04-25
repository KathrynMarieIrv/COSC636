// src/Main/GameSession.java
package Main;

import java.time.Duration;
import java.time.Instant;
import Main.board.Board;
import Main.pieces.Pawn;
import Main.pieces.Piece;
import Main.types.Coordinate;
import Main.types.Team;

public class GameSession {
    private final ClientHandler player1;
    private final ClientHandler player2;
    private final Board board;
    private Team currentTurn;
    private Duration whiteTime = Duration.ZERO;
    private Duration blackTime = Duration.ZERO;
    private Instant turnStart;

    private String fmt(Duration d) {
        long hrs  = d.toHours();
        long mins = d.minusHours(hrs).toMinutes();
        long secs = d.minusHours(hrs).minusMinutes(mins).getSeconds();
        return String.format("%02d:%02d:%02d", hrs, mins, secs);
    }

    public GameSession(ClientHandler player1, ClientHandler player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.board   = new Board();
        this.board.setupBoard();
        this.currentTurn = Team.WHITE;

        // Inform both handlers of the session—and start the game
        player1.setSession(this);
        player2.setSession(this);

        // Initial board broadcast
        broadcastBoard();

        // Kick off the clients
        player1.startGame();
        player2.startGame();
    }

    /**
     * Handle a move request.
     * @return true if the move succeeded, false if invalid.
     */
    public boolean handleMove(Coordinate from, Coordinate to) {
        
        Duration elapsed = Duration.between(turnStart, Instant.now());
        if (currentTurn == Team.WHITE) {
            whiteTime = whiteTime.plus(elapsed);
        } else {
            blackTime = blackTime.plus(elapsed);
        }

        boolean moved = board.movePiece(from, to);
        if (!moved) return false;

        //Pawn promotion check
        Piece movedPiece = board.getPieceAt(to);
            if (movedPiece instanceof Pawn && (to.getY() == 0 || to.getY() == 7)) {
                ClientHandler mover = (currentTurn == Team.WHITE ? player1 : player2);
                mover.sendMessage("PROMOTE: Choose Q, R, B, or N");
                String choice = mover.receiveMessage().trim().toUpperCase();
                board.placePiece(board.makePromotionPiece(choice, currentTurn), to);

                broadcastBoard();
            }

        //Announce check 
        Team opponent = (currentTurn == Team.WHITE ? Team.BLACK : Team.WHITE);
        if (board.isInCheck(opponent)) {
            sendToBoth("CHECK " + opponent.name());
        }

        // Swap turns
        currentTurn = (currentTurn == Team.WHITE ? Team.BLACK : Team.WHITE);

        // Broadcast board and check end-state
        broadcastBoard();
        checkGameOver();
        return true;
    }

    /**
     * Check if it’s the given client’s turn.
     */
    public boolean isPlayerTurn(ClientHandler player) {
        return (currentTurn == Team.WHITE  && player.getColor().equalsIgnoreCase("WHITE"))
            || (currentTurn == Team.BLACK && player.getColor().equalsIgnoreCase("BLACK"));
    }

    public Board getBoard() {
        return board;
    }

    /**
     * Broadcast clocks and board
     */
    private void broadcastBoard() {
        // 1) Black’s clock
        sendToBoth(String.format("Black (%s)", fmt(blackTime)));
    
        for (String line : board.renderLines()) {
            sendToBoth(line);
        }

        sendToBoth(String.format("White (%s)", fmt(whiteTime)));
    
        // Reset turn timer
        turnStart = Instant.now();
    }

    /** Check for checkmate and notify players. */
    private void checkGameOver() {
        
        Team justMoved     = (currentTurn == Team.WHITE ? Team.BLACK : Team.WHITE);
        Team justMovedOpponent = (justMoved == Team.WHITE ? Team.BLACK : Team.WHITE);
    
        // Check for opponent in checkmate
        if (board.isCheckmate(justMovedOpponent)) {
            sendToBoth("GAMEOVER " + justMoved.name());
            endSession();
            return;
        }
    
        // Check for stalemate (no legal moves but not in check)
        if (board.isStalemate(justMovedOpponent)) {
            sendToBoth("GAMEOVER DRAW");
            endSession();
        }
    }
    

    /** Send a one-line message to both players. */
    private void sendToBoth(String msg) {
        player1.sendMessage(msg);
        player2.sendMessage(msg);
    }

    /** Clean-up when game ends. */
    public void endSession() {
        player1.endGame();
        player2.endGame();
    }
}
