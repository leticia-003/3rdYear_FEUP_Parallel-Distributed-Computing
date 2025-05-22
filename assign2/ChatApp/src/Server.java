import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
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
    private final Map<String, Session> tokenToSession = new ConcurrentHashMap<>();


    public Server() {
        rooms.put("General", new Room("General"));
    }

    private Session createSession(String username, Connection connection) {
        String token = UUID.randomUUID().toString();
        long expirationTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes
        return new Session(username, token, connection, null, expirationTime);
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

    private boolean login(Connection connection) throws IOException, ClassNotFoundException {
        connection.write("Username: ");
        String username = connection.read();

        connection.write("Password: ");
        String password = connection.read();

        String[] credentials = database.selectUser(username);

        if(credentials == null || !(username.equals(credentials[0]) && password.equals(credentials[1])) ) {
            System.out.println("Login Failed for user " + username + "\n");
            connection.write("Login Failed for user " + username + "\n");
            return false;
        }
        else {
            System.out.println("Login Successful for user " + username + "\n");
            connection.write("Login Successful for user " + username + "\n");

            connection.setClientName(username);
            return true;
        }
    }

    private void handleAuthentication(Socket clientSocket) {
        Thread.startVirtualThread(() -> {
            try {
                Connection connection = new Connection(clientSocket);

                // 1. Ask client for token or "none"
                connection.write("Send your token if you have one, or 'none' to login/register:\n");
                String token = connection.read().trim();

                Session session = null;

                // 2. Check if token is valid
                if (!"none".equalsIgnoreCase(token)) {
                    session = tokenToSession.get(token);
                    if (session != null && !session.isExpired()) {
                        // Token valid: reassociate connection and restore room
                        session.setConnection(connection);
                        connection.setClientName(session.getUsername());
                        connection.write("Reconnected with token. Resuming session.\n");

                        if (session.getCurrentRoom() != null) {
                            Room room = rooms.get(session.getCurrentRoom());
                            if (room != null) {
                                room.addClientToWatingQueue(connection);
                                connection.write("Rejoined room: " + session.getCurrentRoom() + "\n");
                            }
                        }
                        // Exit method, session resumed
                        return;
                    } else {
                        connection.write("Invalid or expired token. Please login.\n");
                    }
                }

                // 3. No valid token: proceed with login/register as usual
                boolean authenticated = false;

                while (!authenticated) {
                    connection.write("1 - Login\n2 - Register\n3 - Exit\n");
                    String option = connection.read().trim();

                    switch (option) {
                        case "1":
                            authenticated = login(connection);
                            if (!authenticated) {
                                connection.write("Login failed. Try again.\n");
                            }
                            break;
                        case "2":
                            register(connection);
                            authenticated = true;
                            break;
                        case "3":
                            connection.close();
                            return;
                        default:
                            connection.write("Invalid option\n");
                            connection.close();
                            return;
                    }
                }

                if (!authenticated || connection.getClientName() == null) {
                    connection.write("Authentication failed. Disconnecting.\n");
                    connection.close();
                    return;
                }

                // 4. After successful auth, create new session with new token
                session = createSession(connection.getClientName(), connection);
                tokenToSession.put(session.getToken(), session);
                connection.write("Your session token:\n" + session.getToken() + "\n");

                // 5. Let user choose or create room as before
                while (true) {
                    StringBuilder sb = new StringBuilder("Choose a room (new name ⇒ creates it)\n");
                    for (String r : rooms.keySet()) {
                        sb.append("• ").append(r).append('\n');
                    }
                    sb.append("(prefix with \"AI:\" for an AI room)\n");
                    connection.write(sb.toString());

                    String selected = connection.read().trim();

                    Room room = rooms.get(selected);
                    if (room == null) {
                        if (selected.toUpperCase().startsWith("AI:")) {
                            String roomName = selected.substring(3).trim();
                            OllamaClient ollamaClient = new OllamaClient("llama3");
                            room = new AIRoom(roomName, ollamaClient);
                        } else {
                            room = new Room(selected);
                        }
                        rooms.put(selected, room);
                        handleRooms(room);
                    }

                    session.setCurrentRoom(room.getName());
                    room.addClientToWatingQueue(connection);

                    while (true) {
                        String input = connection.read();
                        if (input.equalsIgnoreCase("/leave")) {
                            room.removeClient(connection);
                            room.broadcast("[" + connection.getClientName() + "] left the room.\n");
                            connection.write("You left the room. Returning to lobby.\n");
                            break; // ← Return to room selection loop
                        } else if (input.equalsIgnoreCase("/quit")) {
                            connection.write("Goodbye!\n");
                            connection.close();
                            return;
                        } else {
                            room.enqueueMessage("[" + connection.getClientName() + "]: " + input + "\n");
                        }
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client connection error: " + e.getMessage());
            }
        });
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