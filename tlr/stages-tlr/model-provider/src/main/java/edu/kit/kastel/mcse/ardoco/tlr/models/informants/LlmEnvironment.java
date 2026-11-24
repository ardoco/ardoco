/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import java.util.Objects;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.llm.util.EnvironmentProvider;

/**
 * The environment passed to {@code llm-access}: values come from ARDoCo's {@link Environment} ({@code .env} first,
 * then the system environment), and blank values (e.g., {@code OPENAI_ORGANIZATION_ID=} in a {@code .env} file) count
 * as unset.
 */
public final class LlmEnvironment implements EnvironmentProvider {

    /**
     * The environment backed by ARDoCo's {@link Environment}.
     */
    public static final LlmEnvironment INSTANCE = new LlmEnvironment(Environment::getEnv);

    private final UnaryOperator<@Nullable String> source;

    LlmEnvironment(UnaryOperator<@Nullable String> source) {
        this.source = Objects.requireNonNull(source);
    }

    @Override
    public @Nullable String getenv(String key) {
        String value = source.apply(key);
        return value == null || value.isBlank() ? null : value;
    }
}
