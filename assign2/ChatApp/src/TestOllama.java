public class TestOllama {
    public static void main(String[] args) throws Exception {
        OllamaClient client = new OllamaClient("gemma3:4b"); // or whatever model you have
        String reply = client.generateResponse("Hello!");
        System.out.println("Response from Ollama:\n" + reply);
    }
}