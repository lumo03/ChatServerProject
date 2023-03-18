import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import types.Operation;

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
				ConnectionHandler handler = new ConnectionHandler(this, client, connections.size() + 1);
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
	
	public void broadcast(ConnectionHandler clientH, Operation msgType, List<String> optionalData) {
		String sysMsg = "";
		String pubMsg = "";
		String retMsg = "";
		
		if (msgType == Operation.CMD_QUIT) {
			sysMsg = String.format("%s closed the connection.", clientH.getUsername());
			pubMsg = String.format("%s left the chat.", clientH.getUsername());
			retMsg = "You left the chat.";
		} else if (msgType == Operation.CMD_RENAME) {
			if (optionalData.size() > 0) {
				sysMsg = String.format("%s changed their username to '%s'.", clientH.getUsername(), optionalData.get(0));
				pubMsg = String.format("%s changed their username to '%s'.", clientH.getUsername(), optionalData.get(0));
				retMsg = String.format("Your username was successfully changed to '%s'.", optionalData.get(0));
			} else {
				sysMsg = String.format("%s changed their username.", clientH.getUsername());
				pubMsg = String.format("%s changed their username.", clientH.getUsername());
				retMsg = "Your username was successfully changed.";
			}
		} else if (msgType == Operation.TEXT) {
			if (optionalData.size() > 0) {
				sysMsg = String.format("%s: %s", clientH.getUsername(), optionalData.get(0));
				pubMsg = String.format("%s: %s", clientH.getUsername(), optionalData.get(0));
				retMsg = String.format("YOU: %s", optionalData.get(0));
			} else {
				sysMsg = String.format("%s sent a message.", clientH.getUsername());
				pubMsg = String.format("%s sent a message.", clientH.getUsername());
				retMsg = "Your message was sent.";
			}
		} else if (msgType == Operation.CMD_JOIN) {
			sysMsg = String.format("%s connected.", clientH.getUsername());
			pubMsg = String.format("%s joined the chat.", clientH.getUsername());
			retMsg = String.format("You (%s) are now connected.", clientH.getUsername());
		} else if (msgType == Operation.CMD_ORDER) {
			shoppingCart.addAll(optionalData);
			String items = String.join(", ", optionalData);
			sysMsg = String.format("%s ordered: %s", clientH.getUsername(), items);
			pubMsg = String.format("%s ordered: %s", clientH.getUsername(), items);
			retMsg = String.format("You ordered: %s%nShopping Cart: %s", items, shoppingCart);
		}
		
		System.out.println(sysMsg);
		
		for (ConnectionHandler ch : connections) {
			if (ch != null && ch.getUsername() != clientH.getUsername()) {
				ch.sendMessage(pubMsg);
			}
		}
		
		clientH.sendMessage(retMsg);
	}
	
	public void broadcast(ConnectionHandler clientH, Operation msgType) {
		broadcast(clientH, msgType, List.of());
	}
	
	public void reportError(ConnectionHandler user, Operation operation, List<String> optionalData) {
		System.out.printf("Error at user '%s' while executing operation '%s'.", user.getUsername(), operation);
		if (optionalData.size() > 0) {
			System.out.printf("Additional information: %s%n", optionalData);
		}
		user.sendMessage(String.format("Error while trying to %s. Please try again later.", operation));
	}
	
	public void reportError(ConnectionHandler user, Operation operation) {
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
	
	
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

}
