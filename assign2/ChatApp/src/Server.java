import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * This class implements java Socket server
 *
 */
public class Server {
    public static ServerSocket server;
    public static int port = 4444;
    public static Database database = new Database("assign2/doc/users.txt");

    private final LockedMap<String, Room> rooms = new LockedMap<>();


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

        connection.setClientName(username);
    }

    private void login(Connection connection) throws IOException, ClassNotFoundException {
        connection.write("Username: ");
        String username = connection.read();

        connection.write("Password: ");
        String password = connection.read();

        String[] credentials = database.selectUser(username);

        if(credentials == null || !(username.equals(credentials[0]) && password.equals(credentials[1])) ) {
            System.out.println("Login Failed for user " + username + "\n");
            connection.write("Login Failed for user " + username + "\n");
        }
        else {
            System.out.println("Login Successful for user " + username + "\n");
            connection.write("Login Successful for user " + username + "\n");

            connection.setClientName(username);
        }
    }

    private void handleAuthentication(Socket clientSocket) {

        Runnable newRunnable = () -> {
            try {
                Connection connection = new Connection(clientSocket);

                connection.write("1 - Login\n2- Register\n3 - Exit\n");
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

                // stops unauthenticated users
                if (connection.getClientName() == null) {
                    connection.write("Authentication failed. Disconnecting.\n");
                    connection.close();
                    return;
                }

                while (true) {
                    StringBuilder sb = new StringBuilder("Choose a room (new name ⇒ creates it)\n");
                    rooms.keySet().forEach(r -> sb.append("• ").append(r).append('\n'));
                    sb.append("(prefix with \"AI:\" for an AI room)\n");
                    connection.write(sb.toString());

                    String selected = connection.read().trim();

                    /* ---- 3. Normal room or AI? ---- */
                    Room room = rooms.get(selected);
                    if (room == null) {
                        if (selected.toUpperCase().startsWith("AI:")) {
                            String roomName = selected.substring(3).trim();
                            OllamaClient ollamaClient = new OllamaClient("phi3:3.8b");
                            room = new AIRoom(roomName, ollamaClient);
                        } else {
                            room = new Room(selected);
                        }
                        rooms.put(selected, room);
                        handleRooms(room);
                    }

                    room.addClientToWatingQueue(connection);
                    break;
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client connection error: " + e.getMessage());
            }
        };

        Thread.startVirtualThread(newRunnable);

    }


    /*
        I think the rooms have to deal with
        new clients and
        broadcast messages.
        Which are implemented by Runnables/Thread bellow
     */
    private void handleRooms(Room room) {
        Runnable newClientsHandler = () -> {
            while (true) {
                Connection newClient = room.removeClientFromWatingQueue();
                if (newClient != null) {
                    room.addClient(newClient);
                    room.broadcast("[" + newClient.getClientName() + "] joined the room.\n");
                    room.startListeningFromClient(newClient);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Runnable broadcastHandler = () -> {
            while (true) {
                try {
                    String msg = room.takeMessage();
                    room.broadcast(msg);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Thread.startVirtualThread(newClientsHandler);
        Thread.startVirtualThread(broadcastHandler);

    }



    // Keep accepting new clients and start their threads
    public void listen() throws IOException {
        server = new ServerSocket(port);
        System.out.println("Server listening on port " + port);

        for (Room room : rooms.values()) {
            handleRooms(room);
        }

        while (true) {
            Socket clientSocket = server.accept();
            Thread.startVirtualThread(() -> handleAuthentication(clientSocket));
        }

    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.listen();
    }
}