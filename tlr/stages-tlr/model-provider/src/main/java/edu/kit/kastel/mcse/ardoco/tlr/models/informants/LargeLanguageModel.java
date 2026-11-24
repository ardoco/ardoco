/* Licensed under MIT 2024-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.llm.cache.Cache;
import edu.kit.kastel.mcse.ardoco.llm.cache.chat.ChatCacheKey;
import edu.kit.kastel.mcse.ardoco.llm.chat.CachingChatModel;
import edu.kit.kastel.mcse.ardoco.llm.chat.ChatModelPlatform;
import edu.kit.kastel.mcse.ardoco.llm.chat.ChatModelProvider;
import edu.kit.kastel.mcse.ardoco.llm.chat.LlmConfiguration;

/**
 * The chat models available to ARDoCo. Model creation, authentication, and response caching are provided by
 * the {@code llm-access} library; this enum only selects the platform, model name, and temperature. Ollama
 * token authentication (via {@code OLLAMA_TOKEN}) and an optional {@code OPENAI_ORGANIZATION_ID} are handled
 * by the library. Credentials and hosts are read via {@link LlmEnvironment}.
 */
@Deterministic
public enum LargeLanguageModel {
    // OPENAI
    GPT_5_5("GPT-5.5", ChatModelPlatform.OPENAI, "gpt-5.5-2026-04-23", 1.0), //
    GPT_4_O("GPT-4o", ChatModelPlatform.OPENAI, "gpt-4o-2024-08-06", 0.0), //
    GPT_4_1("GPT-4.1", ChatModelPlatform.OPENAI, "gpt-4.1-2025-04-14", 0.0), //
    GPT_5_4_NANO("GPT-5.4 nano", ChatModelPlatform.OPENAI, "gpt-5.4-nano-2026-03-17", 0.0), //
    GPT_5_4_MINI("GPT-5.4 mini", ChatModelPlatform.OPENAI, "gpt-5.4-mini-2026-03-17", 0.0), //
    GPT_5_4("GPT-5.4", ChatModelPlatform.OPENAI, "gpt-5.4-2026-03-05", 0.0), //
    GPT_5("GPT-5", ChatModelPlatform.OPENAI, "gpt-5-2025-08-07", 1.0), //
    OPENAI_GENERIC(LlmEnvironment.INSTANCE.getenv("OPENAI_MODEL_NAME"), ChatModelPlatform.OPENAI, LlmEnvironment.INSTANCE.getenv("OPENAI_MODEL_NAME"), 0.0), //
    // OLLAMA
    OLLAMA_GENERIC(LlmEnvironment.INSTANCE.getenv("OLLAMA_MODEL_NAME"), ChatModelPlatform.OLLAMA, LlmEnvironment.INSTANCE.getenv("OLLAMA_MODEL_NAME"), 0.0);

    private static final Logger logger = LoggerFactory.getLogger(LargeLanguageModel.class);

    private static final int SEED = loadSeed();

    private final String humanReadableName;
    private final ChatModelPlatform platform;
    private final String modelName;
    private final double temperature;

    LargeLanguageModel(String humanReadableName, ChatModelPlatform platform, String modelName, double temperature) {
        this.humanReadableName = humanReadableName;
        this.platform = platform;
        this.modelName = modelName;
        this.temperature = temperature;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public ChatModel create() {
        ChatModelProvider provider = createProvider();
        Cache<ChatCacheKey> cache = LlmCache.cacheManager().getCache(provider.cacheParameters());
        return new CachingChatModel(provider.createChatModel(), cache);
    }

    public boolean isGeneric() {
        return this.name().endsWith("_GENERIC");
    }

    public boolean isOpenAi() {
        return platform == ChatModelPlatform.OPENAI;
    }

    private ChatModelProvider createProvider() {
        return new ChatModelProvider(LlmConfiguration.builder(platform)
                .modelName(modelName)
                .seed(SEED)
                .temperature(temperature)
                .environment(LlmEnvironment.INSTANCE)
                .build());
    }

    private static int loadSeed() {
        String seedEnv = LlmEnvironment.INSTANCE.getenv("SEED");
        if (seedEnv == null) {
            return 422413373;
        }
        try {
            logger.info("Using SEED: {}", seedEnv);
            return Integer.parseInt(seedEnv);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid SEED environment variable: " + seedEnv, e);
        }
    }
}
