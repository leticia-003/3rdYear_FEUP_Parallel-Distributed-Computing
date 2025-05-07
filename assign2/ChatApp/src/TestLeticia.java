import java.io.IOException;
import java.net.InetAddress;


/*
    I created this Main Class just for the matter of testing the Client - Server relationship
 */

public class TestLeticia {
    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        Client leticia = new Client("Leticia");

        String hostName = InetAddress.getLocalHost().getHostName();
        leticia.connect(hostName, 4444);

        // Reading the menu
        String output;
        output = leticia.read();
        System.out.println(output);

        // Select to login
        leticia.write("1");
        output = leticia.read();

        // Write username
        System.out.println(output);
        leticia.write("victo");

        // Write password
        output = leticia.read();
        System.out.println(output);
        leticia.write("victo");

        // Response of the login
        output = leticia.read();
        System.out.println(output);

        // Selecting a room if passed on the login
        output = leticia.read();
        System.out.println(output);
        leticia.write("General");

    }
}
