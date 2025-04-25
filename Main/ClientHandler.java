// src/Main/ClientHandler.java
package Main;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

import Main.types.Coordinate;
import Main.pieces.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    private String color;
    private boolean inGame = false;
    private ClientHandler opponent;
    private final List<ClientHandler> waitingPlayers;
    private GameSession session;
    private ClientHandler pendingRequestFrom = null;

    public ClientHandler(Socket socket, List<ClientHandler> waitingPlayers) {
        this.socket = socket;
        this.waitingPlayers = waitingPlayers;

        try {
            input  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessage("WAITING_FOR_OPPONENT");
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        try {
            sendMessage("Enter your username:");
            username = input.readLine().trim();
            sendMessage("Welcome " + username + "! Type 'LIST' to view players or 'REQUEST <name>' to challenge.");

            synchronized (waitingPlayers) {
                waitingPlayers.add(this);
            }

            String command;
            while ((command = input.readLine()) != null) {
                String cmd = command.trim();
                System.out.println("[" + username + "] received: " + cmd);

                if (cmd.equalsIgnoreCase("LIST")) {
                    listWaitingPlayers();

                } else if (cmd.toUpperCase().startsWith("REQUEST ")) {
                    System.out.println("Lobby: " +
                        waitingPlayers.stream()
                                      .map(c -> c.username)
                                      .collect(Collectors.joining(", "))
                    );
                    requestMatch(cmd.substring(8));
                
               
                } else if (inGame && cmd.toUpperCase().startsWith("MOVE ")) {
                    String[] parts = cmd.split("\\s+");
                    if (parts.length == 3) {
                        Coordinate from = parseCoordinate(parts[1]);
                        Coordinate to   = parseCoordinate(parts[2]);

                        // 1) Ownership check
                        Piece piece = session.getBoard().getPieceAt(from);
                        if (piece == null ||
                            !piece.getTeam().name().equalsIgnoreCase(getColor())) {
                            sendMessage("Hey! You can only move your own " + getColor() + " pieces.");
                            continue;
                        }

                        // 2) Turn-order check
                        if (!session.isPlayerTurn(this)) {
                            sendMessage("It's not your turn. Please wait.");
                            continue;
                        }

                        // 3) Attempt the move
                        boolean ok = session.handleMove(from, to);
                        if (!ok) {
                            sendMessage("Invalid move. Try again.");
                        }
                    } else {
                        sendMessage("Usage: MOVE <from> <to> (e.g. MOVE e2 e4)");
                    }

                } else if ((cmd.equalsIgnoreCase("YES") || cmd.equalsIgnoreCase("NO")) && pendingRequestFrom != null) {
                    handleResponse(cmd);

                } else if (cmd.equalsIgnoreCase("EXIT")) {
                    break;

                } else {
                    sendMessage("Unknown command.");
                }
            }

        } catch (IOException e) {
            System.out.println(username + " disconnected unexpectedly.");
        } finally {
            cleanUp();
        }
    }

    private Coordinate parseCoordinate(String sq) {
        sq = sq.trim().toLowerCase();
        int x = sq.charAt(0) - 'a';
        int y = Character.getNumericValue(sq.charAt(1)) - 1;
        return new Coordinate(x, y);
    }

    private void listWaitingPlayers() {
        sendMessage("Waiting players:");
        synchronized (waitingPlayers) {
            for (ClientHandler client : waitingPlayers) {
                if (client != this && !client.inGame) {
                    sendMessage("- " + client.username);
                }
            }
        }
    }

    private void requestMatch(String targetName) {
        String targetNorm = targetName.trim().toLowerCase();
        ClientHandler target = null;
        synchronized (waitingPlayers) {
            for (ClientHandler client : waitingPlayers) {
                if (!client.inGame && client.username.trim().toLowerCase().equals(targetNorm)) {
                    target = client;
                    break;
                }
            }
        }
        if (target != null) {
            target.pendingRequestFrom = this;
            target.sendMessage(username + " wants to play. Type YES to accept or NO to decline.");
            sendMessage("Challenge sent to " + target.username);
        } else {
            sendMessage("Player not found or unavailable.");
        }
    }  

    private void handleResponse(String response) {
        if (pendingRequestFrom == null) {
            sendMessage("No pending request.");
            return;
        }
        if (response.equalsIgnoreCase("YES")) {
            this.inGame = true;
            pendingRequestFrom.inGame = true;
            synchronized (waitingPlayers) {
                waitingPlayers.remove(this);
                waitingPlayers.remove(pendingRequestFrom);
            }
            
            this.opponent = pendingRequestFrom;
            pendingRequestFrom.opponent = this;

            // --- ROCK-PAPER-SCISSORS to decide colors ---
            pendingRequestFrom.sendMessage("Let's play rock, paper scissors to determines who moves first! Choose R, P, or S");
            this.sendMessage("Let's play rock, paper scissors to determines who moves first! Choose R, P, or S");

            String choice1 = pendingRequestFrom.receiveChoice();
            String choice2 = this.receiveChoice();
            int result = rpsOutcome(choice1, choice2);

            while (result == 0) {
                pendingRequestFrom.sendMessage("Tie! Try again (R/P/S):");
                this.sendMessage("Tie! Try again (R/P/S):");
                choice1 = pendingRequestFrom.receiveChoice();
                choice2 = this.receiveChoice();
                result  = rpsOutcome(choice1, choice2);
            }

            if (result > 0) {
                pendingRequestFrom.color = "WHITE";
                this.color               = "BLACK";
            } else {
                this.color               = "WHITE";
                pendingRequestFrom.color = "BLACK";
            }

            GameSession g = new GameSession(pendingRequestFrom, this);
            this.setSession(g);
            pendingRequestFrom.setSession(g);

            pendingRequestFrom.sendMessage("YOU ARE " + pendingRequestFrom.color);
            this.sendMessage           ("YOU ARE " + this.color);
            pendingRequestFrom.startGame();
            this.startGame();

        } else {
            pendingRequestFrom.sendMessage(username + " declined your request.");
        }
        pendingRequestFrom = null;
    }

    private String receiveChoice() {
        try {
            String line;
            do {
                line = input.readLine();
                if (line == null) throw new IOException("Client disconnected during RPS");
                line = line.trim().toUpperCase();
            } while (!line.matches("[RPS]"));
            return line;
        } catch (IOException e) {
            e.printStackTrace();
            return "R"; 
        }
    }


        private int rpsOutcome(String a, String b) {
            if (a.equals(b)) return 0;
            return (a.equals("R") && b.equals("S"))
                || (a.equals("P") && b.equals("R"))
                || (a.equals("S") && b.equals("P"))
                ? +1
                : -1;
    }

    
    public void startGame() {
        sendMessage("Game started! You are playing as " + (color != null ? color : "UNASSIGNED"));
        sendMessage("Type moves like: MOVE e2 e4");
    }

    public void endGame() {
        inGame = false;
        opponent = null;
        synchronized (waitingPlayers) {
            waitingPlayers.add(this);
        }
        sendMessage("Game ended. Back to lobby.");
    }

    /**
     * Send message to this client.
     * @return true if successful.
     */
    public boolean sendMessage(String message) {
        output.println(message);
        return !output.checkError();
    }

    /**
     * Read a line sent by this client.
     * @return the received string (without the newline), or null if the client disconnected.
     */
    public String receiveMessage() {
        try {
            return input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return this client’s assigned color (“WHITE” or “BLACK”).
     */
    public String getColor() {
        return color;
    }

    private void cleanUp() {
        try {
            if (inGame && opponent != null) {
                opponent.sendMessage("Your opponent disconnected.");
                opponent.endGame();
            }
            synchronized (waitingPlayers) {
                waitingPlayers.remove(this);
            }
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
