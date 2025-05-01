import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
                        System.out.println(msg);
                    }
                } catch (IOException e) {
                    System.out.println("[ERROR] Lost connection to server.");
                }
            });

            listener.start();

            String input;
            while ((input = userIn.readLine()) != null) {
                serverOut.println(input);
                if (input.equalsIgnoreCase("EXIT")) {
                    break;
                }
            }

            socket.close();
            System.exit(0);

        } catch (IOException e) {
            System.out.println("[ERROR] Could not connect to server.");
            e.printStackTrace();
        }
    }
}

