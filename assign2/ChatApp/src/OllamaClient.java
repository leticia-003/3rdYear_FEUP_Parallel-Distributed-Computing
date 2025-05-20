import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        String response;
        InputStream responseStream;

        int statusCode = connection.getResponseCode();
        if (statusCode >= 400) {
            responseStream = connection.getErrorStream(); // capture error
            try (Scanner scanner = new Scanner(responseStream).useDelimiter("\\A")) {
                String errorResponse = scanner.hasNext() ? scanner.next() : "";
                System.err.println("Ollama API returned error (" + statusCode + "): " + errorResponse);
                throw new RuntimeException("Ollama API call failed with status code: " + statusCode);
            }
        } else {
            responseStream = connection.getInputStream(); // normal response
            try (Scanner scanner = new Scanner(responseStream).useDelimiter("\\A")) {
                response = scanner.hasNext() ? scanner.next() : "";
            }
        }

        Pattern pattern = Pattern.compile("\"response\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"");
        Matcher matcher = pattern.matcher(response);

        if (matcher.find()) {
            return matcher.group(1)
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\"); // unescape backslashes
        } else {
            return "[Bot] Error parsing response";
        }
    }

    private String escapeJson(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}








