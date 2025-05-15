import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;


/*
    I created this Main Class just for the matter of testing the Client - Server relationship
 */

public class TestBia {
    public static void main(String[] args) throws Exception {
        /* ---------- SET‑UP ---------- */
        Scanner stdin = new Scanner(System.in);
        String   host  = InetAddress.getLocalHost().getHostName();

        Client c = new Client("Beatriz");
        c.connect(host, 4444);

        /* ---------- READER thread ---------- */
        Thread reader = Thread.startVirtualThread(() -> {
            try {
                while (true) {
                    String srv = c.read();          // blocks
                    System.out.print(srv);          // show every line from server
                    System.out.flush();
                }
            } catch (Exception e) {
                System.out.println("connection closed: " + e);
            }
        });

        /* ---------- WRITER loop (main thread) ---------- */
        while (true) {
            String line = stdin.nextLine();   // wait for keyboard
            c.write(line);                    // send to server
            if ("/quit".equalsIgnoreCase(line.trim())) {
                c.close();
                break;
            }
        }
    }
}
