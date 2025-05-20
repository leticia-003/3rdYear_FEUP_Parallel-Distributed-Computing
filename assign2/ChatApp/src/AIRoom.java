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
                    client.write("Prompt set. You can start messaging.\n");
                }
                else{
                    client.write("Room prompt is already set.\n");
                    client.write("Room prompt: " + systemPrompt + "\n");
                    client.write("You can start messaging.\n");
                }

                // Normal chat loop
                while (true) {
                    String userMsg = client.read();
                    if (userMsg == null || userMsg.trim().isEmpty()) continue;

                    String userFormatted = "[" + client.getClientName() + "]: " + userMsg;
                    enqueueMessage(userFormatted + "\n");
                    if (conversationHistory.size() < 5){
                        conversationHistory.add(userMsg);
                    }
                    else {
                        conversationHistory.removeFirst();
                        conversationHistory.add(userMsg);
                    }
                    String botReply = buildPrompt(userMsg);
                    enqueueMessage("[Bot]: " + botReply + "\n");
                }
            } catch (Exception e) {
                removeClient(client);
                enqueueMessage("[" + client.getClientName() + "] left the room.\n");
            }
        });

        reader.start();
    }

    private String buildPrompt(String message) {
        // build prompt to pass on to LLM so it sounds more like a personalized chat room
        String prompt = "This is a chat room's prompt and latest messages. Keep them in mind when answering this prompt. Try to sound like a participant in the conversation but don't pretend to be a human. Look out to see if someone asked you a question, answer only the latest one and use the other messages as context.";
        String reply = "";

        prompt += "\nSystem prompt: " + systemPrompt + "\n";

        for (int i = 0; i < conversationHistory.size(); i++){
            prompt += "Message: " + conversationHistory.get(i) + "\n";
        }



        try{
            System.out.println("Sending " + prompt + "...");
            reply = ollamaClient.generateResponse(prompt);
        } catch (Exception e) {
            reply = "An error occurred while generating the reply: " + e.getMessage();
            throw new RuntimeException(e);
        }

        return reply;
    }
}