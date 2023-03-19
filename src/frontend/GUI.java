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
    private final BufferedReader in;
    private final PrintWriter out;
    private Socket client;
    private JPanel panel1;
    private JPanel chatP;
    private JLabel headlineL;
    private JTextArea serverOutputT;
    private JPanel inputP;
    private JTextField inputF;
    private JButton inputB;

    public GUI() {
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(panel1);
        setVisible(true);

        inputF.requestFocus();

        getRootPane().setDefaultButton(inputB);

        inputF.setEnabled(false);
        inputB.setEnabled(false);
        inputB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = inputF.getText();
                System.out.println(input);
                inputF.setText("");
                inputF.requestFocus();
            }
        });

        serverOutputT.append(String.format("Connecting to server...%n"));
        try {
            client = new Socket("127.0.0.1", PORT);
        } catch (IOException e) {
            System.err.println("Connection failed. Reason: " + e.getMessage());
            System.exit(1);
        }

        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = new PrintWriter(client.getOutputStream(), true);

            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        serverOutputT.append(String.format("%s%n", message));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            inputF.setEnabled(true);
            inputB.setEnabled(true);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new GUI();
    }
}
