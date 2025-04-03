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
    // Attributes of the Client
    public String name;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // Constructors of the Client
    public Client() {}
    public Client(String name){
        this.name = name;
    }

    // Methods of the Client
    public void connect(String host, int port) throws IOException, ClassNotFoundException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void write(String msg) throws IOException {
        out.writeObject(msg);
        out.flush();
    }

    public String read() throws IOException, ClassNotFoundException {
        return (String) in.readObject();
    }

    public void close() throws IOException {
        out.close();
        in.close();
        socket.close();
    }

}