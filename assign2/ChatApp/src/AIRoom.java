import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AIRoom extends Room {
    private final String prompt;
    private final OllamaClient aiClient;
    private final List<String> messageHistory = new CopyOnWriteArrayList<>();

    public AIRoom(String name, String prompt, OllamaClient aiClient) {
        super(name);
        this.prompt = prompt;
        this.aiClient = aiClient;
    }

    @Override
    public void enqueueMessage(String message) {
        // Add to history
        messageHistory.add(message);

        // Call super to handle broadcast/queue as normal
        super.enqueueMessage(message);

        // Only respond if the message is not from the Bot
        if (!message.startsWith("[Bot]:")) {
            Thread botResponder = new Thread(() -> {
                try {
                    StringBuilder fullContext = new StringBuilder(prompt + "\n");
                    for (String msg : messageHistory) {
                        fullContext.append(msg).append("\n");
                    }

                    String response = aiClient.generateResponse(fullContext.toString());
                    String botMessage = "[Bot]: " + response + "\n";

                    messageHistory.add(botMessage);
                    super.enqueueMessage(botMessage); // Triggers broadcast via server's broadcast handler
                } catch (Exception e) {
                    super.enqueueMessage("[Bot]: Error generating response.\n");
                }
            });

            botResponder.start();
        }
    }
}