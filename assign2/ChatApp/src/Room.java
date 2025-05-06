import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/*
    A Room is made of a list of users that chat with each other.
    The users are abstracted as the connections they are into with the server
 */
public class Room {
    private final String name;
    private final List<Connection> usersLogged = Collections.synchronizedList(new ArrayList<>());
    private final Queue<Connection> watingQueue = new LinkedList<>();

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
        if(!watingQueue.isEmpty()) {
            return watingQueue.remove();
        }
        return null;
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
}
