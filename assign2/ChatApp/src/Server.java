import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
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

    public static Map<String, String> msgs = new HashMap<>();

    // Constructors of the Server
    public Server() {
        //Init the basic msgs
        msgs.put("Menu", "####### CHAT APP #######\nPlease, log in\nUsername:");
    }

    // Methods of the Server
    public static void write(Socket sock, String msg) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
        oos.writeObject(msg);
        oos.flush();
        oos.close();
    }
    public static String read(Socket sock) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
        String msg = (String) ois.readObject();
        System.out.println(msg);
        ois.close();
        return msg;
    }

    public static void handleSocket(Socket sock) throws IOException {
        String msg = msgs.get("Menu");
        write(sock, msg);
    }

    public static void listen() throws IOException, ClassNotFoundException {
        server = new ServerSocket(port);
        while(true){
            Socket clientSocket = server.accept();
            handleSocket(clientSocket);
            System.out.println("sent");
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("Server listening on port " + port);
        listen();
    }

}