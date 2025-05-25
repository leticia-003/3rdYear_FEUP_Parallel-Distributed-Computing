import java.util.ArrayList;
import java.util.List;

/**
 * Extension of Room that deals specifically with the AI pipeline
 */

public class AIRoom extends Room {
    private String systemPrompt = null;
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


    public void appendToHistory(String msg) {
        if (conversationHistory.size() < 5) {
            conversationHistory.add(msg);
        } else {
            conversationHistory.removeFirst();
            conversationHistory.add(msg);
        }
    }

    public String buildPrompt() {
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