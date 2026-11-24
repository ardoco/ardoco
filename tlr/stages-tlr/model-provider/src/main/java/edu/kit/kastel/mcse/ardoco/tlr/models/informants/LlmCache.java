/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.llm.cache.CacheManager;

/**
 * Holds the {@code llm-access} cache manager used for LLM requests and embeddings: the chat models
 * ({@link LargeLanguageModel}) and the NER embeddings use {@link #cacheManager()}. If no cache directory was
 * configured explicitly, it is configured on first use from the {@code LLM_CACHE_DIR} environment variable
 * (default {@code .cache-llm/}).
 */
public final class LlmCache {

    private static @Nullable CacheManager cacheManager;

    private LlmCache() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Configures the cache directory from the {@code LLM_CACHE_DIR} environment variable, falling back to
     * {@code .cache-llm/} when it is not set.
     */
    public static synchronized void configure() {
        String cacheDir = Environment.getEnv("LLM_CACHE_DIR");
        if (cacheDir == null) {
            cacheDir = ".cache-llm/";
        }
        configure(cacheDir);
    }

    /**
     * Configures the cache directory to the given path, replacing any previous configuration.
     *
     * @param cacheDir The directory in which LLM/embedding caches are stored
     */
    public static synchronized void configure(String cacheDir) {
        try {
            cacheManager = new CacheManager(Path.of(cacheDir), Environment::getEnv);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize the LLM cache directory: " + cacheDir, e);
        }
    }

    /**
     * Configures the cache directory via {@link #configure()} unless it has already been configured.
     */
    public static synchronized void ensureConfigured() {
        if (cacheManager == null) {
            configure();
        }
    }

    /**
     * Returns the cache manager, configuring it first if necessary (see {@link #ensureConfigured()}).
     *
     * @return The cache manager for LLM and embedding caches
     */
    public static synchronized CacheManager cacheManager() {
        ensureConfigured();
        return Objects.requireNonNull(cacheManager);
    }
}
