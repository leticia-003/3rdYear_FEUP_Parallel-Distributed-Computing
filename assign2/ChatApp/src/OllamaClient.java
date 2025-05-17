import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class OllamaClient {
    private final String model;
    private final String endpoint;

    public OllamaClient(String model) {
        this.model = model;
        this.endpoint = "http://localhost:11434/api/generate";
    }

    public String generateResponse(String prompt) throws Exception {
        String jsonPayload = "{ \"model\": \"" + model + "\", \"prompt\": \"" + escapeJson(prompt) + "\", \"stream\": false }";

        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonPayload.getBytes());
        }

        Scanner scanner = new Scanner(connection.getInputStream()).useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        connection.disconnect();

        // manually parse response
        int start = response.indexOf("\"response\":\"") + 11;
        int end = response.indexOf("\",", start);
        if (start < 11 || end < 0) return "[Bot] Error parsing response";
        return response.substring(start, end);
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

