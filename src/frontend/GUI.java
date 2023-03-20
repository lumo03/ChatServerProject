package frontend;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GUI extends JFrame {
    public static final int PORT = 10666;
    private final String fixedHeadlineContent = "GUI";
    private BufferedReader in;
    private PrintWriter out;
    private StringBuilder serverOutput;
    private Socket client;
    private JPanel panel1;
    private JPanel chatP;
    private JLabel headlineL;
    private JTextArea serverOutputT;
    private JPanel inputP;
    private JTextField inputF;
    private JButton inputB;
    private JScrollPane serverOutputS;
    private boolean isConnected;

    public GUI() {
        isConnected = false;

        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(panel1);
        setVisible(true);

        getRootPane().setDefaultButton(inputB);
        inputB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (inputF.getText().length() == 0) {
                    return;
                }
                String input = inputF.getText();
                // System.out.println(input);

                if (input.toUpperCase().startsWith("/RECONNECT")) {
                    reconnect();
                    setup();
                } else {
                    if (!isConnected) {
                        writeToOutput(String.format("You are not connected to the server.%nType `/reconnect to reconnect.%n"));
                    } else {

                        if (input.toUpperCase().startsWith("/CLEAR")) {
                            serverOutput = new StringBuilder();
                            serverOutputT.setText(String.format("%n%n%n"));
                        } else if (input.toUpperCase().startsWith("/QUIT")) {
                            out.println(input);
                            isConnected = false;
                            headlineL.setText(fixedHeadlineContent);
                        } else {
                            out.println(input);
                        }
                    }
                }

                inputF.setText("");
                inputF.requestFocus();
            }
        });

        reconnect();

        setup();
    }

    public static void main(String[] args) {
        new GUI();
    }

    private void setup() {
        inputF.setEnabled(false);
        inputB.setEnabled(false);

        serverOutput = new StringBuilder();

        serverOutputT.append(String.format("%n%n%n"));

        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        writeToOutput(message);

                        if (message.startsWith("You (")) {
                            String username = message.substring(message.indexOf("(") + 1, message.indexOf(")"));

                            if (username.length() > 0) {
                                headlineL.setText(fixedHeadlineContent + " - " + username);
                            }
                        } else if (message.startsWith("Your username was successfully set to '")) {
                            int indexOfOpenQuote = message.indexOf("'");
                            String username = message.substring(indexOfOpenQuote + 1, message.indexOf("'", indexOfOpenQuote + 1));

                            if (username.length() > 0) {
                                headlineL.setText(fixedHeadlineContent + " - " + username);
                            }
                        } else if (message.equals("You left the chat.")) {
                            writeToOutput("Type `/reconnect` to reconnect.");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            inputF.setEnabled(true);
            inputB.setEnabled(true);
            inputF.requestFocus();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToOutput(String message) {
        // remove the last 3 newlines, append the new message and add 3 newlines
        serverOutput.append(String.format("%s%n", message));
        serverOutputT.setText(String.format("%s%n%n%n", serverOutput));
        serverOutputS.getVerticalScrollBar().setValue(serverOutputS.getVerticalScrollBar().getMaximum());
    }

    public void reconnect() {
        if (isConnected) {
            return;
        }

        serverOutputT.append(String.format("(Re-)Connecting to server...%n"));
        try {
            client = new Socket("127.0.0.1", PORT);
            isConnected = true;
        } catch (IOException e) {
            System.err.println("Connection failed. Reason: " + e.getMessage());
            System.exit(1);
        }
    }
}
