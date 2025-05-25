import java.io.IOException;
import java.lang.ClassNotFoundException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


/*
    A Room is made of a list of users that chat with each other.
    The users are abstracted as the connections they are into with the server

    usersLogged : The users that are in the room
    watingQueue : The users that want to get in the room are put in this queue. So whenever the queue is not empty
    it means some one wants to get in the queue, and a broadcast message needs to be triggered
    messageQueue : NOT WORKING YET. But the idea is that every connection/client, would put its message into the
    queue, and with the trigger that the queue it not empty, it would be broadcast to all the other users. Similar
    idea of the above
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



    public Connection removeClientFromWaitingQueue() {
        return waitingQueue.poll();
    }

    public void addClientToWaitingQueue(Connection connection) {
        if (!waitingQueue.snapshot().contains(connection) && !usersLogged.snapshot().contains(connection)) {
            waitingQueue.add(connection);
        }
    }

    public void addClient(Connection connection) {
        if (!usersLogged.snapshot().contains(connection)) {
            usersLogged.add(connection);
        }
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
        return messageQueue.take(); // blocks until message is available
    }

//    public void startListeningFromClient(Connection client) {
//        Thread reader = new Thread(() -> {
//            try {
//                while (true) {
//                    String msg = client.read();
//                    enqueueMessage("[" + client.getClientName() + "]: " + msg + "\n");
//                }
//            } catch (Exception e) {
//                removeClient(client);
//                broadcast("[" + client.getClientName() + "] left the room.\n");
//            }
//        });
//
//        reader.start();
//    }

}
