import java.io.IOException;
import java.net.InetAddress;


/*
    I created this Main Class just for the matter of testing the Client - Server relationship
 */

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        Client victor = new Client("Victor");

        String hostName = InetAddress.getLocalHost().getHostName();
        victor.connect(hostName, 4444);
        System.out.println(victor.read());
        Thread.sleep(5000);
    }
}
