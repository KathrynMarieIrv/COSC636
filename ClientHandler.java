import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    private String color;
    private boolean inGame = false;
    private ClientHandler opponent;
    private List<ClientHandler> waitingPlayers;

    public ClientHandler(Socket socket, List<ClientHandler> waitingPlayers) {
        this.socket = socket;
        this.waitingPlayers = waitingPlayers;

        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            output.println("Enter your username:");
            username = input.readLine();
            output.println("Welcome " + username + "! Type 'LIST' to view players or 'REQUEST <name>' to challenge.");

            synchronized (waitingPlayers) {
                waitingPlayers.add(this);
            }

            String command;
            while ((command = input.readLine()) != null) {
                if (command.equalsIgnoreCase("LIST")) {
                    listWaitingPlayers();
                } else if (command.startsWith("REQUEST ")) {
                    requestMatch(command.substring(8).trim());
                } else if (inGame && command.startsWith("MOVE ")) {
                    sendToOpponent("[MOVE] " + command.substring(5));
                } else if (command.equalsIgnoreCase("YES") || command.equalsIgnoreCase("NO")) {
                    handleResponse(command);
                } else if (command.equalsIgnoreCase("EXIT")) {
                    break;
                } else {
                    output.println("Unknown command.");
                }
            }

        } catch (IOException e) {
            System.out.println(username + " disconnected.");
        } finally {
            cleanUp();
        }
    }

    private void listWaitingPlayers() {
        output.println("Waiting players:");
        synchronized (waitingPlayers) {
            for (ClientHandler client : waitingPlayers) {
                if (client != this && !client.inGame) {
                    output.println("- " + client.username);
                }
            }
        }
    }

    private ClientHandler pendingRequestFrom = null;

    private void requestMatch(String targetName) {
        ClientHandler target = null;
        synchronized (waitingPlayers) {
            for (ClientHandler client : waitingPlayers) {
                if (client.username.equalsIgnoreCase(targetName) && !client.inGame) {
                    target = client;
                    break;
                }
            }
        }

        if (target != null) {
            target.pendingRequestFrom = this;
            target.output.println(username + " wants to play. Type YES to accept or NO to decline.");
        } else {
            output.println("Player not found or unavailable.");
        }
    }

    private void handleResponse(String response) {
        if (pendingRequestFrom == null) {
            output.println("No pending request.");
            return;
        }

        if (response.equalsIgnoreCase("YES")) {
            this.inGame = true;
            pendingRequestFrom.inGame = true;
            synchronized (waitingPlayers) {
                waitingPlayers.remove(this);
                waitingPlayers.remove(pendingRequestFrom);
            }

            new GameSession(this, pendingRequestFrom); // start game session
        } else {
            pendingRequestFrom.output.println(username + " declined your request.");
        }

        pendingRequestFrom = null;
    }

    public void startGame() {
        output.println("Game started! You are playing as " + color);
        output.println("You may type moves like: MOVE e2 e4");
    }

    public void endGame() {
        inGame = false;
        opponent = null;
        synchronized (waitingPlayers) {
            waitingPlayers.add(this);
        }
        output.println("Game ended.");
    }

    public void sendToOpponent(String msg) {
        if (opponent != null) {
            opponent.output.println("[OPPONENT] " + msg);
        }
    }

    public void setOpponent(ClientHandler opp) {
        this.opponent = opp;
    }

    public void setColor(String color) {
        this.color = color;
    }

    private void cleanUp() {
        try {
            if (inGame && opponent != null) {
                opponent.output.println("Your opponent disconnected.");
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
