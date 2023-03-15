import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * 
 */

/**
 * @author Luis Moncada
 *
 */
public class ChatServer {
	
	public static final int PORT = 10666;
    private ServerSocket serverSocket;
    private ArrayList<ChatServerClientThread> clientThreads;

	/**
	 * 
	 */
    public ChatServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            clientThreads = new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error while opening connection on port " + PORT);
            System.exit(1);
        }
    }

    public void run() {
        System.out.println("ChatServer started on port " + PORT);

        while (true) {
            try {
                // wait for client
                Socket newClientSocket = serverSocket.accept();
                
                // when connected, start ClientThread
                var newClient = new ChatServerClientThread(this, newClientSocket);
                clientThreads.add(newClient);
                newClient.start();
                
                System.out.println("Client Nr. " + clientThreads.size() + " connected!");
            } catch (IOException e) {
                System.err.println("Error while connecting to client.");
            }
        }
    }

    public void sendToAll(ChatServerClientThread client, String message) {
        for (var receiver: clientThreads) {
            receiver.send(client.getName() + ": " + message);
        }
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ChatServer().run();
	}

}
