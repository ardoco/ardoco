/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for generating and resetting unique identifiers used throughout the codebase.
 */
public class IdentifierProvider {
    private static final int INITIAL_VALUE = 1337;
    private static final AtomicInteger COUNTER = new AtomicInteger(INITIAL_VALUE);
    private static volatile String extractorPrefix = null;

    private IdentifierProvider() {
        throw new IllegalAccessError("IdentifierProvider is a utility class and cannot be instantiated.");
    }

    /**
     * Creates a new unique identifier string.
     *
     * @return a unique identifier string
     */
    public static String createId() {
        return createId("acm");
    }

    /**
     * Creates a new identifier string. While an extractor is active (see {@link #reset(String)}), its prefix is used. Otherwise, the given default prefix is
     * used. Different prefixes keep ids of different extractors and model kinds disjoint, even though the counter is reset before each extraction.
     *
     * @param defaultPrefix the prefix to use if no extractor prefix is set
     * @return a unique identifier string
     */
    public static String createId(String defaultPrefix) {
        int currentCounter = COUNTER.getAndIncrement();
        String prefix = extractorPrefix != null ? extractorPrefix : defaultPrefix;
        return "%s%09djsd".formatted(prefix, currentCounter);
    }

    /**
     * Resets the identifier counter to its initial value.
     */
    public static void reset() {
        reset(null);
    }

    /**
     * Resets the identifier counter to its initial value and sets the prefix for all ids created until the next reset.
     *
     * @param prefix the prefix of the extractor that is about to run, or null to use the default prefixes
     */
    public static void reset(String prefix) {
        extractorPrefix = prefix;
        COUNTER.set(INITIAL_VALUE);
    }
}
