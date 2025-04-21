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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements java Socket server
 *
 */
public class Server {
    public static ServerSocket server;
    public static int port = 4444;
    public static List<Client> clients = new ArrayList<>();
    public static Database database = new Database("users.txt");
    private final ExecutorService authenticationPool = Executors.newFixedThreadPool(10); // adjust size as needed

    public Server() {}

    private void register(Connection connection) throws IOException, ClassNotFoundException {
        connection.write("Username to register: ");
        String username = connection.read();

        connection.write("Password to register: ");
        String password = connection.read();

        try {
            database.insertUser(username, password);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void login(Connection connection) throws IOException, ClassNotFoundException {
        connection.write("Username: ");
        String username = connection.read();

        connection.write("Password: ");
        String password = connection.read();

        String[] credentials = database.selectUser(username);

        if(credentials == null || !(username.equals(credentials[0]) && password.equals(credentials[1])) ) {
            System.out.println("Login Failed for user " + username);
        }
        else {
            System.out.println("Login Successful for user " + username);
        }
    }

    private void handleAuthentication(Socket clientSocket) {

        Runnable newRunnable = () -> {
            try {
                Connection connection = new Connection(clientSocket);

                connection.write("1 - Login\n2-Register\n3 - Exit");
                String option = connection.read();

                switch (option) {
                    case "1":
                        login(connection);
                        break;
                    case "2":
                        register(connection);
                        break;
                    case "3":
                        connection.close();
                        break;
                    default:
                        System.out.println("Invalid option");
                        connection.close();
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client connection error: " + e.getMessage());
            }
        };

        authenticationPool.submit(newRunnable);
    }

    // Keep accepting new clients and start their threads
    public void listen() throws IOException {
        server = new ServerSocket(port);
        System.out.println("Server listening on port " + port);

        Thread authentication = new Thread(() -> {
            while(true){

                try {
                    Socket clientSocket = server.accept();
                    handleAuthentication(clientSocket);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        // Start the threads
        authentication.start();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.listen();
    }
}