import java.io.IOException;
import java.net.InetAddress;


/*
    I created this Main Class just for the matter of testing the Client - Server relationship
 */
public class TestVictor {
    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        Client victor = new Client("Victor");

        String hostName = InetAddress.getLocalHost().getHostName();
        victor.connect(hostName, 4444);

        // Reading the menu
        String output;
        output = victor.read();
        System.out.println(output);

        // Select to login
        victor.write("1");
        output = victor.read();

        // Write username
        System.out.println(output);
        victor.write("victo");

        // Write password
        output = victor.read();
        System.out.println(output);
        victor.write("victo");

        // Response of the login
        output = victor.read();
        System.out.println(output);

        // Selecting a room if passed on the login
        output = victor.read();
        System.out.println(output);
        victor.write("General");

        System.out.println("Connected. Listening for messages for 1 minute...");

        // Start background thread to read messages from the server
        Thread readerThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String message = victor.read();
                    if (message != null) {
                        System.out.println(message);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Disconnected from server or error reading: " + e.getMessage());
            }
        });

        readerThread.start();

        // Let the client run for 1 minute
        Thread.sleep(60_000);

        // Stop reading and disconnect
        readerThread.interrupt();
        victor.close();
        System.out.println("Disconnected after 1 minute.");
    }
}
