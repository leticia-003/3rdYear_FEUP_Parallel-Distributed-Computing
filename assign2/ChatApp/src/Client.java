import java.io.IOException;
import java.net.Socket;

/**
 * Client class represents a user in the chat application
 */
public class Client {
    // Attributes of the Client
    public String name;
    private Connection connection;

    // Constructors of the Client
    public Client(String name){
        this.name = name;
    }

    /**
     * Connects to the server at the specified host and port.
     *
     * @param host The hostname or IP address of the server.
     * @param port The port number on which the server is listening.
     * @throws IOException If an I/O error occurs when creating the socket.
     * @throws ClassNotFoundException If a class cannot be found during deserialization.
     */
    public void connect(String host, int port) throws IOException, ClassNotFoundException {
        this.connection = new Connection(new Socket(host, port));
    }

    /**
     * Writes a message to the server.
     *
     * @param msg The message to be sent to the server.
     * @throws IOException If an I/O error occurs while writing.
     */
    public void write(String msg) throws IOException {
        this.connection.write(msg);
    }

    /**
     * Reads a message from the server.
     *
     * @return The message read from the server.
     * @throws IOException If an I/O error occurs while reading.
     * @throws ClassNotFoundException If a class cannot be found during deserialization.
     */
    public String read() throws IOException, ClassNotFoundException {
        return this.connection.read();
    }

    /**
     * Closes the connection to the server.
     *
     * @throws IOException If an I/O error occurs while closing the connection.
     */
    public void close() throws IOException {
        this.connection.close();
    }

}