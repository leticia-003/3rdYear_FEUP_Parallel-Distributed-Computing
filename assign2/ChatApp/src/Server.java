import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

/**
 * Server class that handles socket-based multi-room chat functionality.
 *
 * <p>This server allows users to register or log in, join chat rooms, and interact
 * with others in real time. It supports token-based session management to allow
 * fault tolerance — users can reconnect and resume their sessions after disconnections.

 */

public class Server {
    public static ServerSocket server;
    public static int port = 4444;
    public static Database database = new Database("assign2/doc/users.txt");

    private final LockedMap<String, Room> rooms = new LockedMap<>();
    private final LockedMap<String, Session> tokenToSession = new LockedMap<>();

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

        if (credentials == null || !(username.equals(credentials[0]) && password.equals(credentials[1]))) {
            System.out.println("Login Failed for user " + username + "\n");
            connection.write("Login Failed for user " + username + "\n");
            return false;
        } else {
            System.out.println("Login Successful for user " + username + "\n");
            connection.write("Login Successful for user " + username + "\n");
            connection.setClientName(username);
            return true;
        }
    }

    private void handleAuthentication(Socket clientSocket) {
        Thread.startVirtualThread(() -> {
            Connection connection = null;
            Session session = null;
            try {
                connection = new Connection(clientSocket);
                Room room = null;

                String token = connection.read().trim();

                if (!token.isEmpty()) {
                    session = tokenToSession.get(token);
                    if (session != null && !session.isExpired()) {
                        session.setConnection(connection);
                        connection.setClientName(session.getUsername());
                        connection.write("Reconnected. Welcome back, " + session.getUsername() + "!\n");

                        String currentRoom = session.getCurrentRoom();
                        if (currentRoom != null) {
                            room = rooms.get(currentRoom);
                            if (room != null) {
                                room.addClientToWaitingQueue(connection);
                                connection.write("Rejoined room: " + currentRoom + "\n");
                                handleChatLoop(connection, room, session);
                            }
                        }
                    } else {
                        session = null;
                    }
                }

                if (session == null) {
                    connection.write("Please login or register\n");
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
                                connection.write("Goodbye!\n");
                                connection.close();
                                return;
                            default:
                                connection.write("Invalid option\n");
                        }
                    }

                    session = createSession(connection.getClientName(), connection);
                    tokenToSession.put(session.getToken(), session);
                    connection.write("Your session token: " + session.getToken() + "\n");
                }

                while (true) {
                    StringBuilder sb = new StringBuilder("Choose a room (new name ⇒ creates it)\n");
                    for (String r : rooms.keySet()) {
                        sb.append("• ").append(r).append('\n');
                    }
                    sb.append("(prefix with \"AI:\" for an AI room)\n");
                    sb.append("type /quit to exit chat app and /leave to leave chat room.\n");
                    connection.write(sb.toString());

                    String selected = connection.read().trim();
                    if (selected.isEmpty()) continue;

                    Room selectedRoom = rooms.get(selected);
                    if (selectedRoom == null) {
                        if (selected.toUpperCase().startsWith("AI:")) {
                            selectedRoom = new AIRoom(selected, new OllamaClient("llama3"));
                        } else if (selected.equalsIgnoreCase("/leave") || selected.equalsIgnoreCase("/quit")) {
                            connection.write("Invalid room name.\n");
                            continue;
                        } else {
                            selectedRoom = new Room(selected);
                        }
                        rooms.put(selected, selectedRoom);
                        handleRooms(selectedRoom);
                    }

                    session.setCurrentRoom(selectedRoom.getName());
                    selectedRoom.addClientToWaitingQueue(connection);
                    handleChatLoop(connection, selectedRoom, session);
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Client connection error: " + e.getMessage());
            } finally {
                if (session != null && session.getCurrentRoom() != null) {
                    Room currentRoom = rooms.get(session.getCurrentRoom());
                    if (currentRoom != null) {
                        currentRoom.removeClient(connection);
                        currentRoom.broadcast("[" + connection.getClientName() + "] left the room.\n");
                    }
                }
                try {
                    if (connection != null) connection.close();
                } catch (IOException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        });
    }

    private void handleChatLoop(Connection connection, Room room, Session session) throws IOException, ClassNotFoundException {
        AIRoom aiRoom = (room instanceof AIRoom) ? (AIRoom) room : null;

        // Prompt handling for AIRoom
        if (aiRoom != null && aiRoom.getSystemPrompt() == null) {
            connection.write("Enter AI prompt for room \"" + aiRoom.getName() + "\":\n");
            String prompt = connection.read();
            aiRoom.setSystemPrompt(prompt);
            connection.write("Prompt set. You can start messaging.\n");
        } else if (aiRoom != null) {
            connection.write("Room prompt: " + aiRoom.getSystemPrompt() + "\n");
            connection.write("You can start messaging.\n");
        }

        while (true) {
            String input = connection.read();

            if (input == null || input.trim().isEmpty()) continue;

            if (input.equalsIgnoreCase("/leave")) {
                room.removeClient(connection);
                room.broadcast("[" + connection.getClientName() + "] left the room.\n");
                connection.write("You left the room. Returning to lobby.\n");
                session.setCurrentRoom(null);
                break;
            } else if (input.equalsIgnoreCase("/quit")) {
                connection.write("Goodbye!\n");
                connection.close();
                return;
            } else {
                String userMessage = "[" + connection.getClientName() + "]: " + input + "\n";
                room.enqueueMessage(userMessage);

                if (aiRoom != null) {
                    aiRoom.appendToHistory(input);
                    String botReply = aiRoom.buildPrompt();
                    room.enqueueMessage("[Bot]: " + botReply + "\n");
                }
            }
        }
    }

    private void handleRooms(Room room) {
        Runnable newClientsHandler = () -> {
            while (true) {
                Connection newClient = room.removeClientFromWaitingQueue();
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