import backend.Server;
import frontend.GUI;

public class Main {
    Main() {
        new Thread(new Server()).start();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            new GUI();
        }).start();
    }

    public static void main(String[] args) {
        new Main();
    }
}
