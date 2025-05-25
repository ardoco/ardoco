import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String host = System.getenv("OLLAMA_HOST");
        String user = System.getenv("OLLAMA_USER");
        String password = System.getenv("OLLAMA_PASSWORD");

        ChatModel model = OllamaChatModel.builder()
                .baseUrl(host)
                .customHeaders(Map.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8))))
                .modelName("llama3.2:1b")
                .temperature(0.0)
                .build();

        String answer = model.chat("hi");
        System.out.println(answer);

    }
}
