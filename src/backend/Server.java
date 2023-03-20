package backend;

import backend.types.ChatState;
import backend.types.Operation;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    public final static List<String> ORDER_ITEMS = List.of("BURGER", "FRIES", "KETCHUP", "MILKSHAKE", "SODA", "SUNDAE", "WATER", "COKE", "PEPSI", "SPRITE", "MAYONNAISE");
    private final int PORT;
    private final ArrayList<ConnectionHandler> connections;
    private final List<String> shoppingCart;
    private final List<ConnectionHandler> orderParticipants;
    private final int WAITING_FOR_USERS_TO_JOIN_ORDER_TIME = 5000;
    private final int TAKING_THE_ORDER_TIME = 10000;
    private final int DELIVERY_TIME = 12000;
    private final List<String> usernames;
    private ServerSocket server;
    private boolean shouldRun;
    private Executor pool;
    private ChatState chatState;

    public Server(int port) {
        PORT = port;
        connections = new ArrayList<>();
        shouldRun = true;
        shoppingCart = new ArrayList<>();
        chatState = ChatState.CHATTING;
        orderParticipants = new ArrayList<>();
        usernames = new ArrayList<>();
    }

    public Server() {
        this(10666);
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    @Override
    public void run() {
        try {
            pool = Executors.newCachedThreadPool();
            server = new ServerSocket(PORT);

            System.out.println("Server started on port " + PORT);

            while (shouldRun) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(this, client, connections.size() + 1);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            shutdown();
        }
    }

    public ChatState getChatState() {
        return chatState;
    }

    public void setChatState(ChatState chatState) {
        this.chatState = chatState;
    }

    public boolean isUsernameTaken(String username) {
        return usernames.contains(username);
    }

    public boolean addUsername(String username) {
        if (!usernames.contains(username)) {
            usernames.add(username);
            return true;
        } else {
            return false;
        }
    }

    public void removeUsername(String username) {
        usernames.remove(username);
    }

    @Deprecated
    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    /*
     * clientH must not be null
     */
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
                sysMsg = String.format("%s set their username to '%s'.", clientH.getUsername(), optionalData.get(0));
                pubMsg = String.format("%s set their username to '%s'.", clientH.getUsername(), optionalData.get(0));
                retMsg = String.format("Your username was successfully set to '%s'.", optionalData.get(0));
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
        } else if (msgType == Operation.CMD_ADD_TO_ORDER) {
            shoppingCart.addAll(optionalData);
            String items = Utils.joinWithComma(optionalData);
            sysMsg = String.format("%s ordered: %s%nShopping Cart: %s", clientH.getUsername(), items, Utils.formatOrder(shoppingCart));
            pubMsg = String.format("%s ordered: %s%nShopping Cart: %s", clientH.getUsername(), items, Utils.formatOrder(shoppingCart));
            retMsg = String.format("You ordered: %s%nShopping Cart: %s", items, Utils.formatOrder(shoppingCart));
        } else if (msgType == Operation.CMD_REQUEST_ORDER) {
            sysMsg = String.format("%s requested an order.", clientH.getUsername());
            pubMsg = String.format("%s requested an order. "/*+"Enter the command \"/join\" to join the order."*/, clientH.getUsername());
            retMsg = "Your order was requested. "/*+"Waiting for others to join..."*/;
        } else if (msgType == Operation.START_ORDER) {
            sysMsg = "The order was started. Waiting for order.";
            pubMsg = "The order was started. You can now order with \"/add [item1] [item2]\".";
            retMsg = "Your order was started. You can now order with \"/add [item1] [item2]\".";
        } else if (msgType == Operation.ANNOUNCE_DELIVERY_STARTED) {
            sysMsg = String.format("The delivery has started.%nOrder: %s%nDelivery time: %d seconds.", shoppingCart, DELIVERY_TIME / 1000);
            pubMsg = String.format("The delivery has started.%nThe order is: %s.%nIt should arrive in around %d seconds.", Utils.formatOrder(shoppingCart), DELIVERY_TIME / 1000);
            retMsg = String.format("The delivery has started.%nThe order is: %s.%nIt should arrive in around %d seconds.", Utils.formatOrder(shoppingCart), DELIVERY_TIME / 1000);
        } else if (msgType == Operation.ANNOUNCE_DELIVERY_FINISHED) {
            sysMsg = "The delivery has finished.";
            pubMsg = "The delivery has finished.";
            retMsg = "The delivery has finished.";
        }

        // extra line break
//        sysMsg += String.format("%n");
        pubMsg += String.format("%n");
        retMsg += String.format("%n");

        System.out.println(sysMsg);

        for (ConnectionHandler ch : connections) {
            if (ch != null && !ch.getUsername().equals(clientH.getUsername())) {
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
        user.sendMessage(String.format("Error while trying to %s.", operation));
        if (optionalData.size() > 0) {
            user.sendMessage("Reason(s): " + Utils.joinWithComma(optionalData));
        }
        user.sendMessage("Please try again later.");
        user.sendMessage(String.format("%n"));
    }

    public void reportError(ConnectionHandler user, Operation operation) {
        reportError(user, operation, List.of());
    }

    public void startOrder(ConnectionHandler user) {
        new Thread(() -> {
            broadcast(user, Operation.CMD_REQUEST_ORDER);
            chatState = ChatState.WAITING_FOR_USERS_TO_JOIN_ORDER;

            /*
            try {
                Thread.sleep(WAITING_FOR_USERS_TO_JOIN_ORDER_TIME);
            } catch (InterruptedException e) {
                // ignore
            }
            */

            broadcast(user, Operation.START_ORDER);
            chatState = ChatState.TAKING_THE_ORDER;

            try {
                Thread.sleep(TAKING_THE_ORDER_TIME);
            } catch (InterruptedException e) {
                // ignore
            }

            broadcast(user, Operation.ANNOUNCE_DELIVERY_STARTED);
            chatState = ChatState.DELIVERY;

            try {
                Thread.sleep(DELIVERY_TIME);
            } catch (InterruptedException e) {
                // ignore
            }

            broadcast(user, Operation.ANNOUNCE_DELIVERY_FINISHED);
            chatState = ChatState.CHATTING;
        }).start();
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
        } catch (IOException e) {
            // ignore
        }
    }

    public boolean isOrderItem(String s) {
        return ORDER_ITEMS.contains(s.toUpperCase());
    }

}
