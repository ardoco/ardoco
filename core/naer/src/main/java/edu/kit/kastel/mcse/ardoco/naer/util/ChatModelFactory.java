/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.naer.util;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * A factory class for creating different types of {@link ChatModel} instances.
 */
public class ChatModelFactory {
    private static final Logger logger = LoggerFactory.getLogger(ChatModelFactory.class);
    // Input validation is implemented in each method

    private ModelProvider provider;
    private double temperature = 0.0; //default
    private int timeoutSeconds = 60;  //default
    private String modelName = null;

    /**
     * Creates a new factory instance with the specified model provider.
     *
     * @param provider the model provider to use (e.g., OPEN_AI/VDL/LOCAL)
     * @return a new {@link ChatModelFactory} instance configured with the specified provider
     */
    public static ChatModelFactory withProvider(ModelProvider provider) {
        if (provider == null) {
            logger.error("provider is null");
            throw new IllegalArgumentException("Provider cannot be null");
        }

        ChatModelFactory factory = new ChatModelFactory();
        factory.provider = provider;
        return factory;
    }

    /**
     * Sets the temperature parameter for the chat model.
     *
     * @param temperature the temperature value to use
     * @return this factory instance for method chaining
     */
    public ChatModelFactory temperature(double temperature) {
        if (temperature < 0.0) {
            logger.error("temperature must be >= 0.0");
            throw new IllegalArgumentException("Temperature must be >= 0.0");
        }

        this.temperature = temperature;
        return this;
    }

    /**
     * Sets the timeout duration for the chat model operations.
     *
     * @param timeoutSeconds the timeout value in seconds
     * @return this factory instance for method chaining
     */
    public ChatModelFactory timeout(int timeoutSeconds) {
        if (timeoutSeconds < 1) {
            logger.error("timeout must be >= 1");
            throw new IllegalArgumentException("Timeout must be >= 1");
        }
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    /**
     * Sets the specific model name to use for the chat model.
     * <p>
     * Each provider has different available models.
     * If not specified, a default model will be selected based on the provider:
     * <ul>
     * <li>OPEN_AI: "gpt-4.1-nano"</li>
     * <li>VDL: "phi4:latest"</li>
     * <li>LOCAL: N/A (not implemented)</li>
     * </ul>
     *
     * @param modelName the name of the model to use (provider-specific)
     * @return this factory instance for method chaining
     */
    public ChatModelFactory modelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    /**
     * Builds and returns a {@link ChatModel} instance based on the configured provider and settings.
     *
     * @return a configured {@link ChatModel} instance ready for use
     */
    public ChatModel build() {
        return switch (provider) {
        case OPEN_AI -> {
            //most cost efficient models: gpt-4.1-nano https://platform.openai.com/docs/models/gpt-4.1-nano; still pretty cost efficient: gpt-4o-mini https://platform.openai.com/docs/models/gpt-4o-mini
            if (modelName == null)
                modelName = "gpt-4.1-nano"; //default
            yield buildOpenAiModel();
        }
        case LOCAL -> buildLocalModel();
        case OLLAMA -> {
            if (modelName == null)
                modelName = "phi4:latest"; //default
            yield buildOllamaModel();
        }
        };
    }

    /**
     * Builds an OpenAI chat model using the OpenAI API.
     * <p>
     * This method requires the OPENAI_API_KEY environment variable to be set.
     * </p>
     *
     * @return a configured OpenAiChatModel instance
     */
    private ChatModel buildOpenAiModel() {
        String apiKey = Environment.getEnvNonNull("OPENAI_API_KEY");
        return OpenAiChatModel.builder().apiKey(apiKey).timeout(Duration.ofSeconds(timeoutSeconds)).modelName(modelName).temperature(temperature).build();
    }

    /**
     * Builds a {@link ChatModel} for a Ollama instance.
     * <p>
     * This method requires the following environment variables to be set:
     * <ul>
     * <li>OLLAMA_HOST: The base URL of the Ollama server</li>
     * <li>OLLAMA_USER: Username for authentication</li>
     * <li>OLLAMA_PASSWORD: Password for authentication</li>
     * </ul>
     * <p>
     *
     * @return a configured OllamaChatModel instance
     */
    private ChatModel buildOllamaModel() {
        String host = Environment.getEnvNonNull("OLLAMA_HOST");
        String user = Environment.getEnvNonNull("OLLAMA_USER");
        String password = Environment.getEnvNonNull("OLLAMA_PASSWORD");

        var builder = OllamaChatModel.builder().baseUrl(host).modelName(modelName).temperature(temperature).timeout(Duration.ofSeconds(timeoutSeconds));

        if (user != null && password != null) {
            builder = builder.customHeaders(Map.of("Authorization", "Basic " + Base64.getEncoder()
                    .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8))));
        }

        return builder.build();
    }

    /**
     * Builds a local chat model.
     * <p>
     * This method is not yet implemented and will throw an UnsupportedOperationException.
     * It is intended for future support of locally hosted models.
     *
     * @return a local ChatModel instance (not implemented)
     * @throws UnsupportedOperationException always, as this feature is not yet implemented
     */
    private ChatModel buildLocalModel() {
        throw new UnsupportedOperationException("Local model not implemented yet");
    }
}
