import java.net.InetAddress;
import java.util.Scanner;

/*
    I created this Main Class just for the matter of testing the Client - Server relationship
 */

public class TestLeticia{
    public static void main(String[] args) throws Exception {
        Scanner   stdin  = new Scanner(System.in);
        String    host   = InetAddress.getLocalHost().getHostName();

        Client c = new Client();
        c.connect(host, 4444);

        /* ---------- READER thread ---------- */
        Thread reader = Thread.startVirtualThread(() -> {
            try {
                while (true) {
                    String srv = c.read();
                    System.out.print(srv);
                    System.out.flush();
                }
            } catch (Exception e) {
                System.out.println("connection closed: " + e);
            }
        });

        /* ---------- WRITER loop (main thread) ---------- */
        while (true) {
            String user = stdin.nextLine();      // waiting for keyboard
            c.write(user);                       // send to server
            if ("/quit".equalsIgnoreCase(user.trim())) {
                c.close();
                break;
            }
        }
    }
}
