import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

enum MessageType {
	TEXT, CMD_RENAME, CMD_QUIT, CMD_JOIN
}

public class Server implements Runnable {
	
	private final int PORT;
	private ArrayList<ConnectionHandler> connections;
	private ServerSocket server;
	private boolean shouldRun;
	private Executor pool;
	
	public Server(int port) {
		PORT = port;
		connections = new ArrayList<>();
		shouldRun = true;
	}
	
	public Server() {
		this(10666);
	}
	
	@Override
	public void run() {
		try {
			pool = Executors.newCachedThreadPool();
			server = new ServerSocket(PORT);
			
			System.out.println("Server started on port " + PORT);
			
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
	
	@Deprecated
	public void broadcast(String message) {
		for(ConnectionHandler ch : connections) {
			if (ch != null) {
				ch.sendMessage(message);
			}
		}
	}
	
	public void broadcast(ConnectionHandler clientH, MessageType msgType, String[] optionalData) {
		String sysMsg = "";
		String pubMsg = "";
		String retMsg = "";
		
		if (msgType == MessageType.CMD_QUIT) {
			sysMsg = String.format("%s closed the connection.", clientH.getNickname());
			pubMsg = String.format("%s left the chat.", clientH.getNickname());
			retMsg = "You left the chat.";
		} else if (msgType == MessageType.CMD_RENAME) {
			if (optionalData.length > 0) {
				sysMsg = String.format("%s changed their nickname to '%s'.", clientH.getNickname(), optionalData[0]);
				pubMsg = String.format("%s changed their nickname to '%s'.", clientH.getNickname(), optionalData[0]);
				retMsg = String.format("Your nickname was successfully changed to '%s'.", optionalData[0]);
			} else {
				sysMsg = String.format("%s changed their nickname.", clientH.getNickname());
				pubMsg = String.format("%s changed their nickname.", clientH.getNickname());
				retMsg = "Your nickname was successfully changed.";
			}
		} else if (msgType == MessageType.TEXT) {
			if (optionalData.length > 0) {
				sysMsg = String.format("%s: %s", clientH.getNickname(), optionalData[0]);
				pubMsg = String.format("%s: %s", clientH.getNickname(), optionalData[0]);
				retMsg = String.format("YOU: %s", optionalData[0]);
			} else {
				sysMsg = String.format("%s sent a message.", clientH.getNickname());
				pubMsg = String.format("%s sent a message.", clientH.getNickname());
				retMsg = "Your message was sent.";
			}
		} else if (msgType == MessageType.CMD_JOIN) {
			sysMsg = String.format("%s connected.", clientH.getNickname());
			pubMsg = String.format("%s joined the chat.", clientH.getNickname());
			retMsg = "You are now connected.";
		}
		
		System.out.println(sysMsg);
		
		for (ConnectionHandler ch : connections) {
			if (ch != null && ch.getNickname() != clientH.getNickname()) {
				ch.sendMessage(pubMsg);
			}
		}
		
		clientH.sendMessage(retMsg);
	}
	
	public void broadcast(ConnectionHandler clientH, MessageType msgType) {
		broadcast(clientH, msgType, new String[] {});
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
				broadcast(this, MessageType.CMD_JOIN);
				String message;
				
				while((message = in.readLine()) != null) {
					if (message.toUpperCase().startsWith("/QUIT")) {
						broadcast(this, MessageType.CMD_QUIT, new String[] {nickname});
						shutdown();
					} else if (message.toUpperCase().startsWith("/NICKNAME ")) {
						String[] nicknameSplit = message.split(" ", 2);
						if (nicknameSplit.length == 2) {
							broadcast(this, MessageType.CMD_RENAME, new String[] {nicknameSplit[1]});
							nickname = nicknameSplit[1];
						} else {
							out.println("An error occured while trying to change your nickname.");
						}
					} else {
						broadcast(this, MessageType.TEXT, new String[] {message});
					}
				}
				
			} catch(IOException e) {
				shutdown();
			}
		}
		
		public void sendMessage(String message) {
			out.println(message); 
		}
		
		public String getNickname() {
			return nickname;
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
