import java.io.IOException;
import java.net.Socket;

/**
 * Implements java client
 */
public class Client {
    // Attributes of the Client
    public String name;
    private Connection connection;

    // Constructors of the Client
    public Client() {}
    public Client(String name){
        this.name = name;
    }

    // Methods of the Client
    public void connect(String host, int port) throws IOException, ClassNotFoundException {
        this.connection = new Connection(new Socket(host, port));
    }

    public void write(String msg) throws IOException {
        this.connection.write(msg);
    }

    public String read() throws IOException, ClassNotFoundException {
        return this.connection.read();
    }

    public void close() throws IOException {
        this.connection.close();
    }

}