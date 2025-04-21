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

        // Reading the menu
        String output;
        output = victor.read();
        System.out.println(output);

        // Select to login
        victor.write("1");
        output = victor.read();

        // Write username
        System.out.println(output);
        victor.write("tobias");

        // Write password
        output = victor.read();
        System.out.println(output);
        victor.write("victo");

        // Response
        output = victor.read();
        System.out.println(output);

    }
}
