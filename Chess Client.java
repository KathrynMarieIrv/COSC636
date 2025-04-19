import java.io.*;
import java.net.*;

public class ChessClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 12345);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {

            Thread listener = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = serverIn.readLine()) != null) {
                        System.out.println("[SERVER] " + msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            listener.start();

            String input;
            while ((input = userIn.readLine()) != null) {
                serverOut.println(input);
                if (input.equalsIgnoreCase("EXIT")) break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
