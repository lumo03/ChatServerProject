import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 
 */

/**
 * @author Luis Moncada
 *
 */
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

    public void sende(String botschaft) {
        writer.println(botschaft);
        writer.flush();
    }

    @Override
    public void run() {
        String empfangen;
        System.out.println("Started ClientThread with: " + name);
        writer.println("Welcome to Chat-Server! Type `QUIT` to quit. ");
        writer.flush();

        while (true) {
            try {
                empfangen = reader.readLine();
                System.out.println(name + ": " + empfangen);

                if (empfangen == null || empfangen.equalsIgnoreCase("QUIT")) {
                    break;
                }

                server.sendToAll(this, empfangen);
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

        System.out.println("ClientThread closed: " + name);
    }
}
