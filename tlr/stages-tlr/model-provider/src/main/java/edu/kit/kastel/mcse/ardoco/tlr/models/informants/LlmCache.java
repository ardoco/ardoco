/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import java.io.IOException;

import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.llm.cache.CacheManager;

/**
 * Configures the {@code llm-access} cache directory used for LLM requests and embeddings. This must be
 * called once by a runner (or other entry point) before an LLM-based pipeline is executed; the chat models
 * ({@link LargeLanguageModel}) and NER embeddings then use the configured
 * {@link CacheManager#getDefaultInstance() default cache manager}.
 */
public final class LlmCache {

    private LlmCache() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Configures the default cache directory from the {@code LLM_CACHE_DIR} environment variable, falling
     * back to {@code .cache-llm/} when it is not set.
     */
    public static void configure() {
        String cacheDir = Environment.getEnv("LLM_CACHE_DIR");
        if (cacheDir == null) {
            cacheDir = ".cache-llm/";
        }
        configure(cacheDir);
    }

    /**
     * Configures the default cache directory to the given path.
     *
     * @param cacheDir The directory in which LLM/embedding caches are stored
     */
    public static void configure(String cacheDir) {
        try {
            CacheManager.setCacheDir(cacheDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize the LLM cache directory: " + cacheDir, e);
        }
    }
}
