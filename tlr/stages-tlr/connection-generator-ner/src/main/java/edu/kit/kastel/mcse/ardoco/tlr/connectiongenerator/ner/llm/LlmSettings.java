/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.llm;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import edu.kit.kastel.mcse.ardoco.naer.util.ChatModelFactory;
import edu.kit.kastel.mcse.ardoco.naer.util.Environment;

public class LlmSettings {

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
            chatModel = ChatModelFactory.withProvider(modelProvider.getNaerModelProvider())
                    .modelName(modelName)
                    .temperature(temperature)
                    .timeout(timeout)
                    .build();
        }
        return chatModel;
    }

    public EmbeddingModel createEmbeddingModel() {
        if (embeddingModel == null) {
            embeddingModel = switch (embeddingModelProvider) {
                case OPEN_AI -> OpenAiEmbeddingModel.builder().modelName(embeddingModelName).apiKey(Environment.getEnv("OPENAI_API_KEY")).build();
                // TODO what about authentication with ollama.
                case OLLAMA -> OllamaEmbeddingModel.builder().baseUrl(Environment.getEnv("OLLAMA_HOST")).modelName(embeddingModelName).build();
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
