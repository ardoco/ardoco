/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import okhttp3.Credentials;

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

    private final Supplier<ChatLanguageModel> creator;
    private final String humanReadableName;

    LargeLanguageModel(String humanReadableName, Supplier<ChatLanguageModel> creator) {
        this.humanReadableName = humanReadableName;
        this.creator = creator;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public ChatLanguageModel create() {
        return new CachedChatLanguageModel(creator.get(), this.name());
    }

    public boolean isGeneric() {
        return this.name().endsWith("_GENERIC");
    }

    public boolean isOpenAi() {
        return this.name().startsWith("GPT_");
    }

    private static final int SEED = 422413373;

    private static ChatLanguageModel createOpenAiModel(String model) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        String orgId = System.getenv("OPENAI_ORG_ID");
        if (apiKey == null || orgId == null) {
            throw new IllegalArgumentException("OPENAI_API_KEY and OPENAI_ORG_ID must be set as environment variables");
        }
        return new OpenAiChatModel.OpenAiChatModelBuilder().modelName(model)
                .apiKey(apiKey)
                .organizationId(orgId)
                .seed(SEED)
                .temperature(0.0)
                .timeout(Duration.ofMinutes(10))
                .build();
    }

    private static ChatLanguageModel createOllamaModel(String model) {
        String ollamaHost = System.getenv("OLLAMA_HOST");
        String ollamaUser = System.getenv("OLLAMA_USER");
        String ollamaPassword = System.getenv("OLLAMA_PASSWORD");
        if (ollamaHost == null) {
            throw new IllegalArgumentException("OLLAMA_HOST must be set as environment variable");
        }

        OllamaChatModel.OllamaChatModelBuilder builder = new OllamaChatModel.OllamaChatModelBuilder().modelName(model)
                .baseUrl(ollamaHost)
                .seed(SEED)
                .timeout(Duration.ofMinutes(30))
                .temperature(0.0);
        if (ollamaUser != null && ollamaPassword != null) {
            builder.customHeaders(Map.of("Authorization", Credentials.basic(ollamaUser, ollamaPassword)));
        }

        return builder.build();
    }
}
