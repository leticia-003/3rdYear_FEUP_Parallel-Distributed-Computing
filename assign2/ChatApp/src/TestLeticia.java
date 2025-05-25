import java.io.*;
import java.net.InetAddress;
import java.util.Scanner;

public class TestLeticia {

    private static final String USERNAME = "leticia";

    private static final String TOKEN_FILE = System.getProperty("user.home") + File.separator + ".chat_token_" + USERNAME;

    private static String loadToken() {
        File f = new File(TOKEN_FILE);
        if (!f.exists()) return "";
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            return br.readLine().trim();
        } catch (IOException e) {
            return "";
        }
    }

    private static void saveToken(String token) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TOKEN_FILE))) {
            pw.println(token);
        } catch (IOException e) {
            System.err.println("Failed to save token: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        Scanner stdin = new Scanner(System.in);
        String host = InetAddress.getLocalHost().getHostName();

        Client c = new Client(USERNAME);
        c.connect(host, 4444);

        // 1. Load token
        String token = loadToken();
        c.write(token.isEmpty() ? "" : token);

        // 2. Start reader thread
        Thread.startVirtualThread(() -> {
            try {
                while (true) {
                    String srv = c.read();
                    System.out.print(srv);
                    System.out.flush();

                    if (srv.startsWith("Your session token: ")) {
                        String newToken = srv.substring("Your session token: ".length()).trim();
                        if (!newToken.isEmpty()) {
                            saveToken(newToken);
                            System.out.println("[Token saved]");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("connection closed: " + e);
            }
        });

        // 3. Main thread
        while (true) {
            String line = stdin.nextLine();
            c.write(line);
            if ("/quit".equalsIgnoreCase(line.trim())) {
                c.close();
                break;
            }
        }
    }
}
