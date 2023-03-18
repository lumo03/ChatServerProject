import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

import types.Operations;

class ConnectionHandler implements Runnable {
	
	private final Server server;
	private final Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private String nickname;
	
	public ConnectionHandler(Server server, Socket client) {
		this.server = server;
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
			this.server.broadcast(this, Operations.CMD_JOIN);
			String message;
			
			while((message = in.readLine()) != null) {
				if (message.toUpperCase().startsWith("/QUIT")) {
					this.server.broadcast(this, Operations.CMD_QUIT, List.of(nickname));
					shutdown();
				} else if (message.toUpperCase().startsWith("/NICKNAME ")) {
					String[] nicknameSplit = message.split(" ", 2);
					if (nicknameSplit.length == 2) {
						this.server.broadcast(this, Operations.CMD_RENAME, List.of(nicknameSplit[1]));
						nickname = nicknameSplit[1];
					} else {
						this.server.reportError(this, Operations.CMD_RENAME);
					}
				} else if (message.toUpperCase().startsWith("/ADD ")) {
					List<String> orderSplit = Arrays.asList(message.toUpperCase().split(" "));
					orderSplit = orderSplit.stream().filter(w -> this.server.isOrderItem(w)).toList();
					if (orderSplit.size() > 0) {
						this.server.broadcast(this, Operations.CMD_ORDER, orderSplit);
					} else {
						this.server.reportError(this, Operations.CMD_ORDER);
					}
				} else {
					this.server.broadcast(this, Operations.TEXT, List.of(message));
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