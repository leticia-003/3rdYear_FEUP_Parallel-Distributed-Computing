import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements java Socket server
 *
 */
public class Server {
    public static ServerSocket server;
    public static int port = 4444;
    public static Database database = new Database("users.txt");
    private final ExecutorService authenticationPool = Executors.newFixedThreadPool(10); // adjust size as needed
    private final ExecutorService roomsPool = Executors.newFixedThreadPool(10);

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Server() {
        rooms.put("General", new Room("General"));
    }

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
            connection.write("Login Failed for user " + username);
        }
        else {
            System.out.println("Login Successful for user " + username);
            connection.write("Login Successful for user " + username);

            connection.setClientName(username);
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

                // After the users has logged or registed, list the available rooms
                connection.write("\nChoose a room:\n");
                for (String room : rooms.keySet()) {
                    connection.write("-" + room + "\n");
                }

                String selectedRoom = connection.read();
                Room room = rooms.get(selectedRoom);

                room.addClientToWatingQueue(connection);

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client connection error: " + e.getMessage());
            }
        };

        authenticationPool.submit(newRunnable);
    }

    private void handleRooms(Room room) {

        Runnable chatRunnable = () -> {
            while (true) {
                Connection newClient = room.removeClientFromWatingQueue();
                if (newClient != null)
                    room.broadcast("["+newClient.getClientName()+"] : Just entered the room");
            }
        };

        roomsPool.submit(chatRunnable);
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

        for (Room room : rooms.values()) {
            handleRooms(room);
        }

        // Start the threads
        authentication.start();
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.listen();
    }
}