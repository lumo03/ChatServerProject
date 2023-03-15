import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatServerClientThread extends Thread {
    private ChatServer server;
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String name;

    public ChatServerClientThread(ChatServer server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;

        try {
            name = clientSocket.getInetAddress().getCanonicalHostName();
            reader =
                    new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer =
                    new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            // TODO
            System.err.println("Error while writing on client " + name);
            System.exit(2);
        }
    }

    public void send(String message) {
        writer.println(message);
        writer.flush();
    }

    @Override
    public void run() {
        String received;
        System.out.println("ClientThread started with: " + name);
        writer.println("Welcome to Chat-Server! Type `QUIT` to quit.");
        writer.flush();

        while (true) {
            try {
                received = reader.readLine();
                System.out.println(name + ": " + received);

                if (received == null || received.equalsIgnoreCase("QUIT")) {
                    break;
                }

                server.sendToAll(this, received);
            } catch (IOException e) {
                System.err.println("Error while receiving: Client has closed the connection.");
                break;
            }
        }

        try {
            writer.close();
            clientSocket.close();
        } catch (IOException e) {
            // Ignore silently
        }

        System.out.println("Closed ClientThread: " + name);
    }
}