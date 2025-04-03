import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * This class implements java Socket server
 */
public class Server {
    // Attributes of the Server
    public static ServerSocket server;
    public static int port = 4444;
    public static List<Client> clients = new ArrayList<Client>();

    public static Map<String, String> msgs = new HashMap<>() {{
        put("Menu", "####### CHAT APP #######\nPlease, log in\nUsername:");
    }};

    // Constructors of the Server
    public Server() {
        //Init the basic msgs, with the idea of having a map with all the messages the
        //server might have and get them with the key ?
        msgs.put("Menu", "####### CHAT APP #######\nPlease, log in\nUsername:");
    }

    // Methods of the Server
    public static void handleConnection(Socket sock) throws IOException {
        Connection connection = new Connection(sock);
        connection.write("Writted"+msgs.get("Menu"));
    }

    public static void listen() throws IOException, ClassNotFoundException {
        server = new ServerSocket(port);
        while(true){
            Socket clientSocket = server.accept();
            handleConnection(clientSocket);
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Server listening on port " + port);
        listen();
    }

}