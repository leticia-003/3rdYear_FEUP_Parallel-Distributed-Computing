import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/*
    A Room is made of a list of users that chat with each other.
    The users are abstracted as the connections they are into with the server

    usersLogged : The users that are in the room
    watingQueue : The users that want to get in the room are put in this queue. So whenever the queue is not empty
    it means some one wants to get in the queue, and a broadcast message neeeds to be triggered
    messageQueue : NOT WORKING YET. But the idea is that every connection/client, would put its message into the
    queue, and with the trigger that the queue it not empty, it would be broadcast to all the other users. Similar
    idea of the above
 */

public class Room {
    private final String name;
    private final List<Connection> usersLogged = Collections.synchronizedList(new ArrayList<>());
    private final Queue<Connection> watingQueue = new LinkedList<>();
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addClientToWatingQueue(Connection connection) {
        watingQueue.add(connection);
    }

    public Connection removeClientFromWatingQueue() {
        return watingQueue.poll();
    }

    public void addClient(Connection connection) {
        usersLogged.add(connection);
    }

    public void removeClient(Connection connection) {
        usersLogged.remove(connection);
    }

    public void broadcast(String message) {
        synchronized (usersLogged) {
            for (Connection conn : usersLogged) {
                try {
                    conn.write(message);
                } catch (IOException e) {
                    System.out.println("Error broadcasting: " + e.getMessage());
                }
            }
        }
    }

    public void enqueueMessage(String message) {
        messageQueue.offer(message);
    }

    public String takeMessage() throws InterruptedException {
        return messageQueue.take(); // blocks until message is available
    }

    public void startListeningFromClient(Connection client) {
        Thread reader = new Thread(() -> {
            try {
                while (true) {
                    String msg = client.read();
                    enqueueMessage("[" + client.getClientName() + "]: " + msg);
                }
            } catch (Exception e) {
                removeClient(client);
                broadcast("[" + client.getClientName() + "] left the room.");
            }
        });

        reader.start();
    }
}
