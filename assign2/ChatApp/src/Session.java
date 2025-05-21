import java.time.Instant;

public class Session {
    private final String username;
    private final String token;
    private volatile Connection connection;
    private volatile String currentRoom;
    private final long expirationTime;

    public Session(String username, String token, Connection connection, String currentRoom, long expirationTime) {
        this.username = username;
        this.token = token;
        this.connection = connection;
        this.currentRoom = currentRoom;
        this.expirationTime = expirationTime;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(String currentRoom) {
        this.currentRoom = currentRoom;
    }

    public boolean isExpired() {
        return Instant.now().toEpochMilli() > expirationTime;
    }
}
