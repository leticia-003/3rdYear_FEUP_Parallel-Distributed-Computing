import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * Represents a chat room that manages connected clients and message broadcasting.
 *
 * Each room maintains a list of active users, a waiting queue for newly joined users,
 * and a message queue for broadcasting messages. Clients are added to the waiting queue
 * before being admitted to the room and start sending/receiving messages.
 *
 * Typical responsibilities:
 *     - Add/remove users
 *     - Broadcast messages to all users
 *     - Queue and relay messages
 *     - Handle client disconnection gracefully
 */
public class Room {
    private final String name;
    private final LockedList<Connection> usersLogged = new LockedList<>();
    private final LockedQueue<Connection> waitingQueue = new LockedQueue<>();
    private final LockedQueue<String> messageQueue = new LockedQueue<>();


    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addClientToWatingQueue(Connection connection) {
        waitingQueue.add(connection);
    }

    public Connection removeClientFromWatingQueue() {
        return waitingQueue.poll();
    }

    public void addClient(Connection connection) {
        usersLogged.add(connection);
    }

    public void removeClient(Connection connection) {
        usersLogged.remove(connection);
    }

    public void broadcast(String message) {
        for (Connection conn : usersLogged.snapshot()) {
            try {
                conn.write(message);
            } catch (IOException e) {
                System.out.println("Error broadcasting: " + e.getMessage());
            }
        }
    }

    public void enqueueMessage(String message) {
        messageQueue.add(message);

    }

    public String takeMessage() throws InterruptedException {
        return messageQueue.take();
    }

    public void startListeningFromClient(Connection client) {
        Thread reader = new Thread(() -> {
            try {
                while (true) {
                    String msg = client.read();
                    enqueueMessage("[" + client.getClientName() + "]: " + msg + "\n");
                }
            } catch (Exception e) {
                removeClient(client);
                broadcast("[" + client.getClientName() + "] left the room.\n");
            }
        });

        reader.start();
    }

}
