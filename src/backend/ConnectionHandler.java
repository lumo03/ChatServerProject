package backend;

import backend.types.ChatState;
import backend.types.Operation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            sendMessage("Welcome to the Order-Chat-Server!");
            sendMessage("Type `/quit` to leave.");
            sendMessage(String.format("%n"));
            askForUsername();
            server.broadcast(this, Operation.CMD_JOIN);
            sendMessage(String.format("Type `/start` to start the order%n"));
            String message;

            while ((message = in.readLine()) != null) {
                if (message.toUpperCase().startsWith("/QUIT")) {
                    server.broadcast(this, Operation.CMD_QUIT, List.of(username));
                    shutdown();
                } else if (message.toUpperCase().startsWith("/RENAME ")) {
                    String[] usernameSplit = message.split(" ", 2);
                    if (usernameSplit.length == 2) {
                        changeUsername(usernameSplit[1]);
                    } else {
                        server.reportError(this, Operation.CMD_RENAME);
                    }
                } else if (message.toUpperCase().startsWith("/START")) {
                    if (server.getChatState() == ChatState.CHATTING) {
                        server.startOrder(this);
                    } else {
                        server.reportError(this, Operation.CMD_ADD_TO_ORDER, List.of("No order was started or the order is already in progress."));
                    }
                } else if (message.toUpperCase().startsWith("/ADD ")) {
                    if (server.getChatState() == ChatState.TAKING_THE_ORDER) {
                        List<String> orderSplit = Arrays.stream(message.toUpperCase().split(" "))
                                .filter(server::isOrderItem)
                                .collect(Collectors.toList());
                        if (orderSplit.size() > 0) {
                            server.broadcast(this, Operation.CMD_ADD_TO_ORDER, orderSplit);
                        } else {
                            server.reportError(this, Operation.CMD_ADD_TO_ORDER);
                        }
                    } else {
                        server.reportError(this, Operation.CMD_ADD_TO_ORDER, List.of("No order was started."));
                    }
                } else if (message.toUpperCase().startsWith("/REMOVE ")) {
                    if (server.getChatState() == ChatState.TAKING_THE_ORDER) {
                        List<String> orderSplit = Arrays.stream(message.toUpperCase().split(" "))
                                .filter(server::isOrderItem)
                                .collect(Collectors.toList());
                        if (orderSplit.size() > 0) {
                            server.broadcast(this, Operation.CMD_REMOVE_FROM_ORDER, orderSplit);
                        } else {
                            server.reportError(this, Operation.CMD_REMOVE_FROM_ORDER);
                        }
                    } else {
                        server.reportError(this, Operation.CMD_ADD_TO_ORDER, List.of("No order was started."));
                    }
                } else {
                    server.broadcast(this, Operation.TEXT, List.of(message));
                }
            }

        } catch (IOException e) {
            shutdown();
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getUsername() {
        return username;
    }

    public boolean changeUsername(String newUsername) {
        if (server.isUsernameTaken(newUsername)) {
            server.reportError(this, Operation.CMD_RENAME, List.of("Username is already taken."));
            return false;
        } else {
            username = newUsername;
            server.addUsername(newUsername);
            server.broadcast(this, Operation.CMD_RENAME, List.of(username));
            return true;
        }
    }

    public void askForUsername() {
        String wantedUsername = username;
        boolean isValid;
        do {
            sendMessage("Provide a username:");
            try {
                wantedUsername = in.readLine();
            } catch (IOException e) {
                server.reportError(this, Operation.CMD_RENAME, List.of("Could not read username from input stream."));
            }

            if (wantedUsername.toUpperCase().startsWith("/QUIT")) {
                server.broadcast(this, Operation.CMD_QUIT, List.of(username));
                shutdown();
                return;
            }

            isValid = !server.isUsernameTaken(wantedUsername);

            if (!isValid) {
                server.reportError(this, Operation.CMD_RENAME, List.of("Username is already taken."));
            }
        } while (!isValid);

        changeUsername(wantedUsername);
        sendMessage("Your username is now: " + wantedUsername);
    }

    public void shutdown() {
        try {
            in.close();
            out.close();
            if (client.isClosed()) {
                server.broadcast(this, Operation.CMD_QUIT);
                client.close();
            }
        } catch (IOException e) {
            // ignore
        }
        server.removeUsername(username);
    }

}