import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Implements java client
 */
public class Client {

    public Client() {}

    public static void connect(String host, int port) throws IOException{
        Socket socket = new Socket(host, port);

        //write into the socket
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        //read from the socket
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        oos.writeObject("Hello... Im Client");
        oos.flush();

        ois.close();
        oos.close();
    }

    public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException, InterruptedException{
        String host = InetAddress.getLocalHost().getHostName();
        connect(host, 4444);
    }
}