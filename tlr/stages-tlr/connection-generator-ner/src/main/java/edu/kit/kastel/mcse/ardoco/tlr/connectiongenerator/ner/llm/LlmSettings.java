/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.llm;

import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ardoco.naer.util.ChatModelFactory;

public class LlmSettings {

    private final ModelProvider modelProvider;
    private final String modelName;
    private final double temperature;
    private final int timeout;

    protected LlmSettings(ModelProvider modelProvider, String modelName, double temperature, int timeout) {
        this.modelProvider = modelProvider;
        this.modelName = modelName;
        this.temperature = temperature;
        this.timeout = timeout;
    }

    public static LlmSettings getDefaultSettings() {
        return new LlmSettings(ModelProvider.OPEN_AI, "gpt-4.1", 0.3, 120);
    }

    public ChatModel createChatModel() {
        return ChatModelFactory.withProvider(modelProvider.getNaerModelProvider()).modelName(modelName).temperature(temperature).timeout(timeout).build();
    }

    public static class Builder {
        private ModelProvider modelProvider = null;
        private String modelName = null;
        private double temperature = 0.3;
        private int timeout = 120;

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

        public LlmSettings build() {
            if (modelName == null || modelProvider == null) {
                throw new IllegalArgumentException("Not all required parameters are properly set, yet.");
            }
            return new LlmSettings(modelProvider, modelName, temperature, timeout);
        }
    }
}
