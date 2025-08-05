/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.llm;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import edu.kit.kastel.mcse.ardoco.naer.util.Environment;

public class LlmSettings {

    public static final int DEFAULT_SEED = 1995820773;
    private ChatModel chatModel = null;
    private EmbeddingModel embeddingModel = null;

    private final ModelProvider modelProvider;
    private final String modelName;
    private final ModelProvider embeddingModelProvider;
    private final String embeddingModelName;
    private final double temperature;
    private final int timeout;

    protected LlmSettings(ModelProvider modelProvider, String modelName, double temperature, ModelProvider embeddingModelProvider, String embeddingModelName,
            int timeout) {
        this.modelProvider = modelProvider;
        this.modelName = modelName;
        this.temperature = temperature;
        this.timeout = timeout;
        this.embeddingModelProvider = embeddingModelProvider;
        this.embeddingModelName = embeddingModelName;
    }

    public static LlmSettings getDefaultSettings() {
        return new Builder().build();
    }

    public synchronized ChatModel createChatModel() {
        if (chatModel == null) {
            if (Objects.requireNonNull(modelProvider) == ModelProvider.OPEN_AI) {
                chatModel = buildOpenAiChatModel();
            } else if (modelProvider == ModelProvider.OLLAMA) {
                chatModel = buildOllamaModel();
            }
        }
        return chatModel;
    }

    private OpenAiChatModel buildOpenAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(getOpenaiApiKey())
                .timeout(Duration.ofSeconds(timeout))
                .modelName(modelName)
                .temperature(temperature)
                .seed(DEFAULT_SEED)
                .build();
    }

    private static String getOpenaiApiKey() {
        return Environment.getEnv("OPENAI_API_KEY");
    }

    private ChatModel buildOllamaModel() {
        String user = Environment.getEnvNonNull("OLLAMA_USER");
        String password = Environment.getEnvNonNull("OLLAMA_PASSWORD");

        var builder = OllamaChatModel.builder().baseUrl(getOllamaHost()).modelName(modelName).temperature(temperature).timeout(Duration.ofSeconds(timeout));

        if (user != null && password != null) {
            builder = builder.customHeaders(
                    Map.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8))));
        }

        return builder.build();
    }

    private static String getOllamaHost() {
        return Environment.getEnv("OLLAMA_HOST");
    }

    public EmbeddingModel createEmbeddingModel() {
        if (embeddingModel == null) {
            embeddingModel = switch (embeddingModelProvider) {
                case OPEN_AI -> OpenAiEmbeddingModel.builder().modelName(embeddingModelName).apiKey(getOpenaiApiKey()).build();
                // TODO what about authentication with ollama.
                case OLLAMA -> OllamaEmbeddingModel.builder().baseUrl(getOllamaHost()).modelName(embeddingModelName).build();
            };
        }
        return embeddingModel;
    }

    public static class Builder {
        private ModelProvider modelProvider = ModelProvider.OPEN_AI;
        private String modelName = "gpt-4.1";
        private double temperature = 0.3;
        private int timeout = 120;
        private ModelProvider embeddingModelProvider = ModelProvider.OPEN_AI;
        private String embeddingModelName = "text-embedding-3-large";

        public Builder modelProvider(ModelProvider modelProvider) {
            this.modelProvider = modelProvider;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder embeddingModelProvider(ModelProvider embeddingModelProvider) {
            this.embeddingModelProvider = embeddingModelProvider;
            return this;
        }

        public Builder embeddingModelName(String embeddingModelName) {
            this.embeddingModelName = embeddingModelName;
            return this;
        }

        public LlmSettings build() {
            if (modelName == null || modelProvider == null) {
                throw new IllegalArgumentException("Not all required parameters are properly set, yet.");
            }
            return new LlmSettings(modelProvider, modelName, temperature, embeddingModelProvider, embeddingModelName, timeout);
        }
    }
}
