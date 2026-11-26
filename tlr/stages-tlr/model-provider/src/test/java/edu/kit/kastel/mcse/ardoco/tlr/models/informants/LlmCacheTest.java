/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.llm.cache.chat.ChatCacheParameter;

/**
 * Tests the configuration of the LLM cache. The cache manager is static state, so every test starts and ends unconfigured.
 */
class LlmCacheTest {

    @TempDir
    Path cacheDir;

    @BeforeEach
    @AfterEach
    void reset() {
        LlmCache.reset();
    }

    @Test
    void blankCacheDirectoryFallsBackToDefault() {
        assertEquals(LlmCache.DEFAULT_CACHE_DIRECTORY, LlmCache.cacheDirectory(null));
        assertEquals(LlmCache.DEFAULT_CACHE_DIRECTORY, LlmCache.cacheDirectory(""));
        assertEquals(LlmCache.DEFAULT_CACHE_DIRECTORY, LlmCache.cacheDirectory("  "));
        assertEquals("my-cache", LlmCache.cacheDirectory("my-cache"));
    }

    @Test
    void ensureConfiguredKeepsAnExplicitDirectory() {
        LlmCache.configure(cacheDir.toString());
        var configured = LlmCache.cacheManager();

        LlmCache.ensureConfigured();

        assertSame(configured, LlmCache.cacheManager());
        var cache = LlmCache.cacheManager().getCache(new ChatCacheParameter("test-model", 1, 0.0));
        cache.put("prompt", "answer");
        cache.flush();
        assertTrue(Files.isRegularFile(cacheDir.resolve("Chat_test-model_1.json")));
    }

    @Test
    void cacheManagerConfiguresItselfOnFirstUse() throws IOException {
        Path expectedDir = Path.of(LlmCache.cacheDirectory(Environment.getEnv("LLM_CACHE_DIR")));
        Path cacheFile = expectedDir.resolve("Chat_first-use-test_1.json");
        Files.deleteIfExists(cacheFile);

        try {
            var cache = LlmCache.cacheManager().getCache(new ChatCacheParameter("first-use-test", 1, 0.0));
            cache.put("prompt", "answer");
            cache.flush();

            assertTrue(Files.isRegularFile(cacheFile), () -> "Missing " + cacheFile.toAbsolutePath());
        } finally {
            Files.deleteIfExists(cacheFile);
        }
    }

    @Test
    void createWorksWithoutExplicitConfiguration() {
        Assumptions.assumeTrue(Environment.getEnv("OPENAI_API_KEY") != null, "OPENAI_API_KEY is only set (to a dummy) by the surefire configuration");
        // Creating the model sends no request.
        assertNotNull(LargeLanguageModel.GPT_4_O.create());
    }

}
