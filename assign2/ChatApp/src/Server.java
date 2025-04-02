import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class implements java Socket server
 */
public class Server {
    public static ServerSocket server;
    public static int port = 4444;

    public static void listen() throws IOException, ClassNotFoundException {
        server = new ServerSocket(port);

        while(true){
            System.out.println("Waiting for the client request");
            Socket socket = server.accept();

            //read from socket
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            //write to socket
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            String message = (String) ois.readObject();
            System.out.println("Message Received: " + message);

            ois.close();
            oos.close();
            socket.close();

            //terminate the server if client sends exit request
            if(message.equalsIgnoreCase("exit")) break;
        }
        System.out.println("Shutting down Socket server!!");
        server.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException{
        listen();
    }

}