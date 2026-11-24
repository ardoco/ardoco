/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
    void cacheManagerConfiguresItselfOnFirstUse() {
        assertNotNull(LlmCache.cacheManager());
        String configuredDir = LlmEnvironment.INSTANCE.getenv("LLM_CACHE_DIR");
        if (configuredDir != null) {
            assertTrue(Files.isDirectory(Path.of(configuredDir)));
        }
    }

    @Test
    void createWorksWithoutExplicitConfiguration() {
        // OPENAI_API_KEY is a dummy (see the surefire configuration); creating the model sends no request.
        assertNotNull(LargeLanguageModel.GPT_4_O.create());
    }

    @Test
    void blankEnvironmentValuesAreUnset() {
        var environment = new LlmEnvironment(Map.of("BLANK", " ", "EMPTY", "", "SET", "value")::get);
        assertNull(environment.getenv("BLANK"));
        assertNull(environment.getenv("EMPTY"));
        assertNull(environment.getenv("MISSING"));
        assertEquals("value", environment.getenv("SET"));
    }
}
