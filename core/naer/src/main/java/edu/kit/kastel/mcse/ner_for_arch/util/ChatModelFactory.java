package edu.kit.kastel.mcse.ner_for_arch.util;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

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
     *     <li>OPEN_AI: "gpt-4.1-nano"</li>
     *     <li>VDL: "phi4:latest"</li>
     *     <li>LOCAL: N/A (not implemented)</li>
     * </ul>
     * </p>
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

    /**
     * Builds an OpenAI chat model using the OpenAI API.
     * <p>
     * This method requires the OPENAI_API_KEY environment variable to be set.
     * </p>
     *
     * @return a configured OpenAiChatModel instance
     */
    private ChatModel buildOpenAiModel() {
        logger.warn("Using OpenAi chat model => can be expensive!");
        String apiKey = System.getenv("OPENAI_API_KEY");
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    /**
     * Builds a {@link ChatModel} using the KIT SDQ Virtual Design Lab (VDL) Server infrastructure, which hosts an ollama instance.
     * <p>
     * This method requires the following environment variables to be set:
     * <ul>
     *   <li>OLLAMA_HOST: The base URL of the Ollama server</li>
     *   <li>OLLAMA_USER: Username for authentication</li>
     *   <li>OLLAMA_PASSWORD: Password for authentication</li>
     * </ul>
     * <p>
     * For more information about the Virtual Design Lab Server, see:
     * <a href="https://sdq.kastel.kit.edu/wiki/Virtual_Design_Lab_Server">Virtual Design Lab Server Wiki</a>
     *
     * @return a configured OllamaChatModel instance
     */
    private ChatModel buildVdlModel() {
        String host = System.getenv("OLLAMA_HOST");
        String user = System.getenv("OLLAMA_USER");
        String password = System.getenv("OLLAMA_PASSWORD");

        return OllamaChatModel.builder()
                .baseUrl(host)
                .customHeaders(Map.of("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8))))
                .modelName(modelName)
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
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
