package util;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

//TODO das benutzen (ChatModelBuilder ist dann deprecated)) + javadoc
public class ChatModelFactory {
    private ModelProvider provider;
    private double temperature = 0.0;
    private String modelName = "phi4:latest";

    public static ChatModelFactory withProvider(ModelProvider provider) {
        ChatModelFactory factory = new ChatModelFactory();
        factory.provider = provider;
        return factory;
    }

    public ChatModelFactory temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public ChatModelFactory modelName(String modelName) {
        if (modelName != null && !modelName.isBlank()) {
            this.modelName = modelName;
        }
        return this;
    }

    public ChatModel build() {
        return switch (provider) {
            case OPEN_AI -> buildOpenAiModel();
            case LOCAL -> buildLocalModel();
            case VDL -> buildVdlModel();
        };
    }

    private ChatModel buildOpenAiModel() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        //TODO log warn "achtung kann teuer werden"
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    private ChatModel buildVdlModel() {
        String host = System.getenv("OLLAMA_HOST");
        String user = System.getenv("OLLAMA_USER");
        String password = System.getenv("OLLAMA_PASSWORD");

        return OllamaChatModel.builder()
                .baseUrl(host)
                .customHeaders(Map.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8))))
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    private ChatModel buildLocalModel() {
        throw new UnsupportedOperationException("Local model not implemented yet");
    }
}

