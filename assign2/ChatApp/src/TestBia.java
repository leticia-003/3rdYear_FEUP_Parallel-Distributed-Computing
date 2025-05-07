import java.io.IOException;
import java.net.InetAddress;


/*
    I created this Main Class just for the matter of testing the Client - Server relationship
 */

public class TestBia {
    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        Client bia = new Client("Bia");

        String hostName = InetAddress.getLocalHost().getHostName();
        bia.connect(hostName, 4444);

        // Reading the menu
        String output;
        output = bia.read();
        System.out.println(output);

        // Select to login
        bia.write("1");
        output = bia.read();

        // Write username
        System.out.println(output);
        bia.write("bia");

        // Write password
        output = bia.read();
        System.out.println(output);
        bia.write("bia");

        // Response of the login
        output = bia.read();
        System.out.println(output);

        // Selecting a room if passed on the login
        output = bia.read();
        System.out.println(output);
        bia.write("General");

    }
}
