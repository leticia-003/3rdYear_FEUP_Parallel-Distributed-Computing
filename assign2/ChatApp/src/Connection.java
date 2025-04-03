import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(this.socket.getOutputStream());
        this.in = new ObjectInputStream(this.socket.getInputStream());
    }

    public String read() throws IOException, ClassNotFoundException {
        return (String) this.in.readObject();
    }

    public void write(String msg) throws IOException {
        this.out.writeObject(msg);
        this.out.flush();
    }

    public void close() throws IOException {
        this.socket.close();
        this.out.close();
        this.in.close();
    }
}
