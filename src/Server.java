import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server implements Runnable {
	
	private ArrayList<ConnectionHandler> connections;
	private ServerSocket server;
	private boolean shouldRun;
	private Executor pool;
	
	public Server() {
		connections = new ArrayList<>();
		shouldRun = true;
	}
	
	@Override
	public void run() {
		try {
			pool = Executors.newCachedThreadPool();
			server = new ServerSocket(10666);
			
			System.out.println("Server started on port " + server.getLocalPort());
			
			while(shouldRun) {
				Socket client = server.accept();
				ConnectionHandler handler = new ConnectionHandler(client);
				connections.add(handler);
				pool.execute(handler);
			}
		} catch (IOException e) {
			shutdown();
		}
	}
	
	public void broadcast(String message) {
		for(ConnectionHandler ch : connections) {
			if (ch != null) {
				ch.sendMessage(message);
			}
		}
	}
	
	public void shutdown() {
		try {
			shouldRun = false;
			if (!server.isClosed()) {
				server.close();
			}
			for (ConnectionHandler ch : connections) {
				if (ch != null) {
					ch.shutdown();
				}
			}
		} catch(IOException e) {
			// ignore
		}
	}
	
	
	class ConnectionHandler implements Runnable {
		
		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		private String nickname;
		
		public ConnectionHandler(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				out = new PrintWriter(client.getOutputStream(), true);
				out.println("Welcome to the Chat-Server!");
				out.flush();
				out.println("Provide a nickname: ");
				out.flush();
				nickname = in.readLine();
				System.out.println(nickname + " connected!");
				broadcast(nickname + " joined the chat!");
				String message;
				
				while((message = in.readLine()) != null) {
					if (message.toUpperCase().startsWith("/QUIT")) {
						System.out.println(nickname + " closed the connection!");
						broadcast(nickname + " left the chat!");
						shutdown();
					} else if (message.toUpperCase().startsWith("/NICKNAME ")) {
						String[] nicknameSplit = message.split(" ", 2);
						if (nicknameSplit.length == 2) {
							System.out.println(nickname + " changed their nickname to '" + nicknameSplit[1] + "'");
							broadcast(nickname + " changed their nickname to '" + nicknameSplit[1] + "'");
							nickname = nicknameSplit[1];
							out.println("You succesfully changed your nickname to '" + nickname + "'");
						} else {
							out.println("An error occured while trying to change your nickname.");
						}
					} else {
						System.out.println(nickname + ": " + message);
						broadcast(nickname + ": " + message);
					}
				}
				
			} catch(IOException e) {
				shutdown();
			}
		}
		
		public void sendMessage(String message) {
			out.println(message); 
		}
		
		public void shutdown() {
			try {
				in.close();
				out.close();
				if(client.isClosed()) {
					client.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
		
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

}
