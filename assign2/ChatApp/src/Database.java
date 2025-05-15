import java.io.*;
import java.util.Scanner;

public class Database {
    private File file;

    public Database(String filePath) {
        this.file = new File(filePath);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create DB file: " + file.getAbsolutePath(), e);
        }
    }

    public void insertUser(String username, String password) throws IOException {
        try (FileWriter writer = new FileWriter(file, true);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.write(username + ":" + password);
            bufferedWriter.newLine();
        }
    }

    public String[] selectUser(String username) throws IOException {
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                if (parts.length == 2 && parts[0].equals(username)) {
                    return parts; // parts[0] = username, parts[1] = password
                }
            }
        }
        return null; // User not found
    }
}