import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

enum Operations {
	TEXT, CMD_RENAME, CMD_QUIT, CMD_JOIN, CMD_ORDER;
	
	@Override
	public String toString() {
		switch (this) {
			case TEXT:
				return "Text";
			case CMD_RENAME:
				return "Rename";
			case CMD_QUIT:
				return "Quit";
			case CMD_JOIN:
				return "Join";
			case CMD_ORDER:
				return "Order";
			default:
				throw new IllegalArgumentException();
		}
	}
}

public class Server implements Runnable {
	
	private final int PORT;
	private ArrayList<ConnectionHandler> connections;
	private ServerSocket server;
	private boolean shouldRun;
	private Executor pool;
	public final static List<String> ORDER_ITEMS = List.of("BURGER", "FRIES", "KETCHUP");
	private List<String> shoppingCart;
	
	public Server(int port) {
		PORT = port;
		connections = new ArrayList<>();
		shouldRun = true;
		shoppingCart = new ArrayList<>();
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
	
	public void broadcast(ConnectionHandler clientH, Operations msgType, List<String> optionalData) {
		String sysMsg = "";
		String pubMsg = "";
		String retMsg = "";
		
		if (msgType == Operations.CMD_QUIT) {
			sysMsg = String.format("%s closed the connection.", clientH.getNickname());
			pubMsg = String.format("%s left the chat.", clientH.getNickname());
			retMsg = "You left the chat.";
		} else if (msgType == Operations.CMD_RENAME) {
			if (optionalData.size() > 0) {
				sysMsg = String.format("%s changed their nickname to '%s'.", clientH.getNickname(), optionalData.get(0));
				pubMsg = String.format("%s changed their nickname to '%s'.", clientH.getNickname(), optionalData.get(0));
				retMsg = String.format("Your nickname was successfully changed to '%s'.", optionalData.get(0));
			} else {
				sysMsg = String.format("%s changed their nickname.", clientH.getNickname());
				pubMsg = String.format("%s changed their nickname.", clientH.getNickname());
				retMsg = "Your nickname was successfully changed.";
			}
		} else if (msgType == Operations.TEXT) {
			if (optionalData.size() > 0) {
				sysMsg = String.format("%s: %s", clientH.getNickname(), optionalData.get(0));
				pubMsg = String.format("%s: %s", clientH.getNickname(), optionalData.get(0));
				retMsg = String.format("YOU: %s", optionalData.get(0));
			} else {
				sysMsg = String.format("%s sent a message.", clientH.getNickname());
				pubMsg = String.format("%s sent a message.", clientH.getNickname());
				retMsg = "Your message was sent.";
			}
		} else if (msgType == Operations.CMD_JOIN) {
			sysMsg = String.format("%s connected.", clientH.getNickname());
			pubMsg = String.format("%s joined the chat.", clientH.getNickname());
			retMsg = "You are now connected.";
		} else if (msgType == Operations.CMD_ORDER) {
			shoppingCart.addAll(optionalData);
			String items = String.join(", ", optionalData);
			sysMsg = String.format("%s ordered: %s", clientH.getNickname(), items);
			pubMsg = String.format("%s ordered: %s", clientH.getNickname(), items);
			retMsg = String.format("You ordered: %s%nShopping Cart: %s", items, shoppingCart);
		}
		
		System.out.println(sysMsg);
		
		for (ConnectionHandler ch : connections) {
			if (ch != null && ch.getNickname() != clientH.getNickname()) {
				ch.sendMessage(pubMsg);
			}
		}
		
		clientH.sendMessage(retMsg);
	}
	
	public void broadcast(ConnectionHandler clientH, Operations msgType) {
		broadcast(clientH, msgType, List.of());
	}
	
	public void reportError(ConnectionHandler user, Operations operation, List<String> optionalData) {
		System.out.printf("Error at user '%s' while executing operation '%s'.", user.getNickname(), operation);
		if (optionalData.size() > 0) {
			System.out.printf("Additional information: %s%n", optionalData);
		}
		user.out.printf("Error while trying to %s. Please try again later.%n", operation);
	}
	
	public void reportError(ConnectionHandler user, Operations operation) {
		reportError(user, operation, List.of());
	}
	
	public void shutdown() {
		try {
			shouldRun = false;
			if (server != null && !server.isClosed()) {
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
	
	public boolean isOrderItem(String s) {
		return ORDER_ITEMS.contains(s.toUpperCase());
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
				out.print("Provide a nickname: ");
				out.flush();
				nickname = in.readLine();
				broadcast(this, Operations.CMD_JOIN);
				String message;
				
				while((message = in.readLine()) != null) {
					if (message.toUpperCase().startsWith("/QUIT")) {
						broadcast(this, Operations.CMD_QUIT, List.of(nickname));
						shutdown();
					} else if (message.toUpperCase().startsWith("/NICKNAME ")) {
						String[] nicknameSplit = message.split(" ", 2);
						if (nicknameSplit.length == 2) {
							broadcast(this, Operations.CMD_RENAME, List.of(nicknameSplit[1]));
							nickname = nicknameSplit[1];
						} else {
							reportError(this, Operations.CMD_RENAME);
						}
					} else if (message.toUpperCase().startsWith("/ADD ")) {
						List<String> orderSplit = Arrays.asList(message.toUpperCase().split(" "));
						orderSplit = orderSplit.stream().filter(w -> isOrderItem(w)).toList();
						if (orderSplit.size() > 0) {
							broadcast(this, Operations.CMD_ORDER, orderSplit);
						} else {
							reportError(this, Operations.CMD_ORDER);
						}
					} else {
						broadcast(this, Operations.TEXT, List.of(message));
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
