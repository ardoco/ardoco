/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.function.Supplier;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;

/**
 * Holder for a concrete {@link PersistenceHandler} (Neo4j) so pipeline stages can access persistence statically.
 *
 * <p>Persistence is optional: {@link #runQuietly} / {@link #callQuietly} isolate Neo4j failures so a down
 * database does not abort the pipeline. Dual-write is transitional; Neo4j sole store is the end vision.
 */
public class PersistenceBridge extends AbstractConfigurable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceBridge.class);

    private static final PersistenceBridge INSTANCE = new PersistenceBridge();

    private static PersistenceHandler handler;

    public static boolean usePersistenceStatic = false;

    public static boolean persistTextStateStatic = false;

    public static boolean persistRecommendationsStatic = false;

    @Configurable
    private boolean usePersistence = false;

    /**
     * When true (and {@link #isAvailable()}), TextState noun mappings are dual-written to Neo4j.
     * Default false so existing persistence tests stay unchanged.
     */
    @Configurable
    private boolean persistTextState = false;

    /**
     * When true (and {@link #isAvailable()}), RecommendedInstances are dual-written to Neo4j.
     * Default false. Implies {@link #persistTextState}: enabling recommendations without TextState would leave
     * {@code HAS_NAME_MAPPING} / {@code HAS_TYPE_MAPPING} edges unresolved.
     */
    @Configurable
    private boolean persistRecommendations = false;

    private PersistenceBridge() {
        // Private constructor for Singleton
    }

    public static PersistenceBridge getInstance() {
        return INSTANCE;
    }

    public static void setHandler(PersistenceHandler newHandler) {
        handler = newHandler;
    }

    public static PersistenceHandler getHandler() {
        return usePersistenceStatic ? handler : null;
    }

    public static boolean isAvailable() {
        return usePersistenceStatic && handler != null;
    }

    /**
     * @return true if Neo4j persistence is available and TextState dual-write is enabled
     */
    public static boolean shouldPersistTextState() {
        return isAvailable() && persistTextStateStatic;
    }

    /**
     * @return true if Neo4j persistence is available and recommendation dual-write is enabled
     */
    public static boolean shouldPersistRecommendations() {
        return isAvailable() && persistRecommendationsStatic;
    }

    /**
     * Runs a persistence side-effect. On {@link RuntimeException} (e.g. Neo4j down), logs and continues.
     * Does not catch {@link Error}.
     */
    public static void runQuietly(String operation, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            LOGGER.warn("Persistence operation '{}' failed; continuing without Neo4j. Cause: {}", operation, ex.toString());
            LOGGER.debug("Persistence failure details for '{}'", operation, ex);
        }
    }

    /**
     * Runs a persistence call that returns a value. On failure, logs and returns {@code fallback}.
     */
    public static <T> T callQuietly(String operation, Supplier<T> action, T fallback) {
        try {
            return action.get();
        } catch (RuntimeException ex) {
            LOGGER.warn("Persistence operation '{}' failed; using fallback. Cause: {}", operation, ex.toString());
            LOGGER.debug("Persistence failure details for '{}'", operation, ex);
            return fallback;
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(ImmutableSortedMap<String, String> additionalConfiguration) {
        usePersistenceStatic = this.usePersistence;
        if (this.persistRecommendations && !this.persistTextState) {
            LOGGER.info("persistRecommendations is enabled without persistTextState; enabling persistTextState automatically");
            this.persistTextState = true;
        }
        persistTextStateStatic = this.persistTextState;
        persistRecommendationsStatic = this.persistRecommendations;
    }
}
