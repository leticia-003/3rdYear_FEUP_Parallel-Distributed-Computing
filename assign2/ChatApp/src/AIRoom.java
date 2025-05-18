import java.util.ArrayList;
import java.util.List;

public class AIRoom extends Room {
    private String systemPrompt = null; // Prompt starts as null
    private final OllamaClient ollamaClient;
    private final List<String> conversationHistory = new ArrayList<>();

    public AIRoom(String name, OllamaClient client) {
        super(name);
        this.ollamaClient = client;
    }

    public void setSystemPrompt(String prompt) {
        this.systemPrompt = prompt;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public OllamaClient getOllamaClient() {
        return ollamaClient;
    }

    @Override
    public void startListeningFromClient(Connection client) {
        Thread reader = new Thread(() -> {
            try {
                // Only ask for prompt if it hasn't been set yet
                if (systemPrompt == null) {
                    client.write("Enter AI prompt for room \"" + getName() + "\":\n");
                    String prompt = client.read();
                    setSystemPrompt(prompt);
                }

                // Normal chat loop
                while (true) {
                    String userMsg = client.read();
                    if (userMsg == null || userMsg.trim().isEmpty()) continue;

                    String userFormatted = "[" + client.getClientName() + "]: " + userMsg;
                    enqueueMessage(userFormatted + "\n");
                    conversationHistory.add(userFormatted);

                    String botReply = ollamaClient.generateResponse(userMsg);
                    enqueueMessage("[Bot]: " + botReply + "\n");
                }
            } catch (Exception e) {
                removeClient(client);
                enqueueMessage("[" + client.getClientName() + "] left the room.\n");
            }
        });

        reader.start();
    }
}