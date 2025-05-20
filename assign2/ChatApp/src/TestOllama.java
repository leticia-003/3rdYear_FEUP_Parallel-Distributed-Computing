public class TestOllama {
    public static void main(String[] args) throws Exception {
        OllamaClient client = new OllamaClient("llama3");
        String reply = client.generateResponse("Hello!");
        System.out.println("Response from Ollama:\n" + reply);
    }
}