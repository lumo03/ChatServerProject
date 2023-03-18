import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import types.Operations;

class ConnectionHandler implements Runnable {
	
	private final Server server;
	private final Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private String username;
	
	public ConnectionHandler(Server server, Socket client, int id) {
		this.server = server;
		this.client = client;
		username = "USER-" + id;
	}

	@Override
	public void run() {
		try {
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
			out.println("Welcome to the Order-Chat-Server!");
			out.println("Type `/quit` to leave.");
			out.flush();
			out.print("Provide a username: ");
			out.flush();
			String input = in.readLine();
			if (input != null && input.toUpperCase().contains("/QUIT")) {
				shutdown();
			} else if (input != null && !input.strip().isEmpty()) {
				// the case when the connection is closed but the readLine() is still
				// running, is not handled
				System.out.println("username changed, length: "+input.length());
				username = input;
			}
			server.broadcast(this, Operations.CMD_JOIN);
			String message;
			
			while((message = in.readLine()) != null) {
				if (message.toUpperCase().startsWith("/QUIT")) {
					server.broadcast(this, Operations.CMD_QUIT, List.of(username));
					shutdown();
				} else if (message.toUpperCase().startsWith("/RENAME ")) {
					String[] usernameSplit = message.split(" ", 2);
					if (usernameSplit.length == 2) {
						server.broadcast(this, Operations.CMD_RENAME, List.of(usernameSplit[1]));
						username = usernameSplit[1];
					} else {
						server.reportError(this, Operations.CMD_RENAME);
					}
				} else if (message.toUpperCase().startsWith("/ADD ")) {
					List<String> orderSplit = Arrays.stream(message.toUpperCase().split(" "))
                            .filter(w -> server.isOrderItem(w))
                            .collect(Collectors.toList());
					if (orderSplit.size() > 0) {
						server.broadcast(this, Operations.CMD_ORDER, orderSplit);
					} else {
						server.reportError(this, Operations.CMD_ORDER);
					}
				} else {
					server.broadcast(this, Operations.TEXT, List.of(message));
				}
			}
			
		} catch(IOException e) {
			shutdown();
		}
	}
	
	public void sendMessage(String message) {
		out.println(message); 
	}
	
	public String getUsername() {
		return username;
	}
	
	public void shutdown() {
		try {
			in.close();
			out.close();
			if(client.isClosed()) {
				server.broadcast(this, Operations.CMD_QUIT);
				client.close();
			}
		} catch (IOException e) {
			// ignore
		}
	}
	
}