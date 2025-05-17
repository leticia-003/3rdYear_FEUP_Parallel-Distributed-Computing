public class AIRoom extends Room {
    private String systemPrompt = null;
    private final OllamaClient ollamaClient;

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
                // Ask for prompt first time
                client.write("Enter AI prompt for room \"" + getName() + "\":\n");
                String prompt = client.read();
                setSystemPrompt(prompt);

                // Generate initial response and send it
                String botReply = ollamaClient.generateResponse(prompt);
                enqueueMessage("[Bot]: " + botReply + "\n");

                // Then enter normal chat loop
                while (true) {
                    String userMsg = client.read();
                    if (userMsg == null || userMsg.trim().isEmpty()) continue;

                    String userFormatted = "[" + client.getClientName() + "]: " + userMsg;
                    enqueueMessage(userFormatted + "\n");

                    botReply = ollamaClient.generateResponse(userMsg);
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