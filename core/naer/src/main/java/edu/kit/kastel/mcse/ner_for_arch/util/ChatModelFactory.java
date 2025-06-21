package edu.kit.kastel.mcse.ner_for_arch.util;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

//TODO javadoc + args checken und exceptions
public class ChatModelFactory {
    private ModelProvider provider;
    private double temperature = 0.0; //default
    private String modelName = null;

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
            case OPEN_AI -> {
                //most coste efficient models: gpt-4.1-nano https://platform.openai.com/docs/models/gpt-4.1-nano; still pretty cost efficient: gpt-4o-mini https://platform.openai.com/docs/models/gpt-4o-mini
                if (modelName == null) modelName = "gpt-4.1-nano"; //default
                yield buildOpenAiModel();
            }
            case LOCAL -> {
                yield buildLocalModel();
            }
            case VDL -> {
                if (modelName == null) modelName = "phi4:latest"; //default
                yield buildVdlModel();
            }
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

