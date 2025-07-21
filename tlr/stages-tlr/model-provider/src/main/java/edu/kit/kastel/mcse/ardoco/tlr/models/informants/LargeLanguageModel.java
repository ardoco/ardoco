/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.function.Supplier;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public enum LargeLanguageModel {
    // OPENAI
    GPT_4_O_MINI("GPT-4o mini", () -> createOpenAiModel("gpt-4o-mini-2024-07-18")), //
    GPT_4_O("GPT-4o", () -> createOpenAiModel("gpt-4o-2024-08-06")), //
    GPT_4_TURBO("GPT-4 Turbo", () -> createOpenAiModel("gpt-4-turbo-2024-04-09")), //
    GPT_4("GPT-4", () -> createOpenAiModel("gpt-4-0613")), //
    GPT_3_5_TURBO("GPT-3.5 Turbo", () -> createOpenAiModel("gpt-3.5-turbo-0125")), //
    OPENAI_GENERIC(System.getenv("OPENAI_MODEL_NAME"), () -> createOpenAiModel(System.getenv("OPENAI_MODEL_NAME"))), //
    // OLLAMA
    CODELLAMA_13B("Codellama 13b", () -> createOllamaModel("codellama:13b")), //
    // CODELLAMA_70B("Codellama 70b", () -> createOllamaModel("codellama:70b")), //
    //
    // GEMMA_2_27B("Gemma2 27b", () -> createOllamaModel("gemma2:27b")), //
    //
    // QWEN_2_72B("Qwen2 72b", () -> createOllamaModel("qwen2:72b")), //
    //
    LLAMA_3_1_8B("Llama3.1 8b", () -> createOllamaModel("llama3.1:8b-instruct-fp16")), //
    LLAMA_3_1_70B("Llama3.1 70b", () -> createOllamaModel("llama3.1:70b")), //
    //
    // MISTRAL_7B("Mistral 7b", () -> createOllamaModel("mistral:7b")), //
    // MISTRAL_NEMO_27B("Mistral Nemo 12b", () -> createOllamaModel("mistral-nemo:12b")), //
    // MIXTRAL_8_X_22B("Mixtral 8x22b", () -> createOllamaModel("mixtral:8x22b")), //
    //
    // PHI_3_14B("Phi3 14b", () -> createOllamaModel("phi3:14b")), //
    //
    OLLAMA_GENERIC(System.getenv("OLLAMA_MODEL_NAME"), () -> createOllamaModel(System.getenv("OLLAMA_MODEL_NAME")));

    private final Supplier<ChatModel> creator;
    private final String humanReadableName;

    LargeLanguageModel(String humanReadableName, Supplier<ChatModel> creator) {
        this.humanReadableName = humanReadableName;
        this.creator = creator;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public ChatModel create() {
        return new CachedChatLanguageModel(creator.get(), this.name());
    }

    public boolean isGeneric() {
        return this.name().endsWith("_GENERIC");
    }

    public boolean isOpenAi() {
        return this.name().startsWith("GPT_");
    }

    private static final int SEED = 422413373;

    /**
     * Creates an OpenAI chat model instance.
     * Requires OpenAI organization ID and API key to be set in environment variables.
     *
     * @param model The name of the model to use
     * @return A configured OpenAI chat model instance
     * @throws IllegalStateException If required environment variables are not set
     */
    private static OpenAiChatModel createOpenAiModel(String model) {
        String openAiOrganizationId = System.getenv("OPENAI_ORGANIZATION_ID");
        String openAiApiKey = System.getenv("OPENAI_API_KEY");
        if (openAiOrganizationId == null || openAiApiKey == null) {
            throw new IllegalStateException("OPENAI_ORGANIZATION_ID or OPENAI_API_KEY environment variable not set");
        }
        return new OpenAiChatModel.OpenAiChatModelBuilder().modelName(model)
                .organizationId(openAiOrganizationId)
                .apiKey(openAiApiKey)
                .temperature(0.0)
                .seed(SEED)
                .build();
    }

    private static OllamaChatModel createOllamaModel(String model) {
        String host = System.getenv("OLLAMA_HOST");
        String user = System.getenv("OLLAMA_USER");
        String password = System.getenv("OLLAMA_PASSWORD");

        var ollama = OllamaChatModel.builder().baseUrl(host).modelName(model).timeout(Duration.ofMinutes(15)).temperature(0.0).seed(SEED);
        if (user != null && password != null && !user.isEmpty() && !password.isEmpty()) {
            ollama.customHeaders(Map.of("Authorization", "Basic " + Base64.getEncoder()
                    .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8))));
        }
        return ollama.build();
    }
}
