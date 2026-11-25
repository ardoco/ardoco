/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;

/**
 * Guards the cache identity of the models: changing the platform, model name, seed or temperature of a constant would
 * silently stop using its existing cache file.
 */
class LargeLanguageModelTest {

    private static final Map<LargeLanguageModel, String> CACHE_FILES = Map.of(//
            LargeLanguageModel.GPT_5_5, "Chat_gpt-5.5-2026-04-23_422413373_1.0.json", //
            LargeLanguageModel.GPT_4_O, "Chat_gpt-4o-2024-08-06_422413373.json", //
            LargeLanguageModel.GPT_4_1, "Chat_gpt-4.1-2025-04-14_422413373.json", //
            LargeLanguageModel.GPT_5_4_NANO, "Chat_gpt-5.4-nano-2026-03-17_422413373.json", //
            LargeLanguageModel.GPT_5_4_MINI, "Chat_gpt-5.4-mini-2026-03-17_422413373.json", //
            LargeLanguageModel.GPT_5_4, "Chat_gpt-5.4-2026-03-05_422413373.json", //
            LargeLanguageModel.GPT_5, "Chat_gpt-5-2025-08-07_422413373_1.0.json");

    @TempDir
    Path cacheDir;

    @BeforeEach
    void configure() {
        LlmCache.configure(cacheDir.toString());
    }

    @AfterEach
    void reset() {
        LlmCache.reset();
    }

    @ParameterizedTest
    @EnumSource(value = LargeLanguageModel.class, mode = Mode.EXCLUDE, names = { "OPENAI_GENERIC", "OLLAMA_GENERIC" })
    void cacheFileNameIsStable(LargeLanguageModel model) {
        Assumptions.assumeTrue(Environment.getEnv("SEED") == null, "SEED overrides the default seed");
        String expected = CACHE_FILES.get(model);
        assertNotNull(expected, "Add the cache file name of " + model);
        assertTrue(model.isOpenAi());

        var cache = LlmCache.cacheManager().getCache(model.cacheParameters());
        cache.put("prompt", "answer");
        cache.flush();

        assertTrue(Files.isRegularFile(cacheDir.resolve(expected)), () -> "Missing " + expected);
    }
}
