/* Licensed under MIT 2024-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.model.chat.ChatModel;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.llm.cache.Cache;
import edu.kit.kastel.mcse.ardoco.llm.cache.CacheManager;
import edu.kit.kastel.mcse.ardoco.llm.cache.chat.ChatCacheKey;
import edu.kit.kastel.mcse.ardoco.llm.chat.CachingChatModel;
import edu.kit.kastel.mcse.ardoco.llm.chat.ChatModelPlatform;
import edu.kit.kastel.mcse.ardoco.llm.chat.ChatModelProvider;
import edu.kit.kastel.mcse.ardoco.llm.chat.LlmConfiguration;

/**
 * The chat models available to ARDoCo. Model creation, authentication, and response caching are provided by
 * the {@code llm-access} library; this enum only selects the platform, model name, and temperature. Ollama
 * token authentication (via {@code OLLAMA_TOKEN}) and an optional {@code OPENAI_ORGANIZATION_ID} are handled
 * by the library.
 */
@Deterministic
public enum LargeLanguageModel {
    // OPENAI
    GPT_4_O("GPT-4o", ChatModelPlatform.OPENAI, "gpt-4o-2024-08-06", 0.0), //
    GPT_4_1("GPT-4.1", ChatModelPlatform.OPENAI, "gpt-4.1-2025-04-14", 0.0), //
    GPT_5("GPT-5", ChatModelPlatform.OPENAI, "gpt-5-2025-08-07", 1.0), //
    OPENAI_GENERIC(Environment.getEnv("OPENAI_MODEL_NAME"), ChatModelPlatform.OPENAI, Environment.getEnv("OPENAI_MODEL_NAME"), 0.0), //
    // OLLAMA
    OLLAMA_GENERIC(Environment.getEnv("OLLAMA_MODEL_NAME"), ChatModelPlatform.OLLAMA, Environment.getEnv("OLLAMA_MODEL_NAME"), 0.0);

    private static final Logger logger = LoggerFactory.getLogger(LargeLanguageModel.class);

    private static final int SEED = loadSeed();
    private static final CacheManager CACHE_MANAGER = createCacheManager();

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
        Cache<ChatCacheKey> cache = CACHE_MANAGER.getCache(this, provider.cacheParameters());
        return new CachingChatModel(provider.createChatModel(), cache);
    }

    public ChatModel createUncached() {
        return createProvider().createChatModel();
    }

    public boolean isGeneric() {
        return this.name().endsWith("_GENERIC");
    }

    public boolean isOpenAi() {
        return this.name().startsWith("GPT_");
    }

    private ChatModelProvider createProvider() {
        return new ChatModelProvider(LlmConfiguration.builder(platform).modelName(modelName).seed(SEED).temperature(temperature).build());
    }

    private static int loadSeed() {
        String seedEnv = Environment.getEnv("SEED");
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

    private static CacheManager createCacheManager() {
        String cacheDir = Environment.getEnv("LLM_CACHE_DIR");
        if (cacheDir == null) {
            cacheDir = ".cache-llm/";
        }
        try {
            return new CacheManager(Path.of(cacheDir));
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize LLM cache directory: " + cacheDir, e);
        }
    }
}
