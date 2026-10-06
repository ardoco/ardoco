/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
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

    public static boolean persistNerConnectionStatic = false;

    /**
     * When true, {@link #runQuietly} and {@link #callQuietly} rethrow {@link RuntimeException} instead of swallowing.
     * Integration tests enable this so persistence bugs fail tests instead of returning fallbacks.
     */
    public static boolean failOnPersistenceErrorStatic = false;

    /** Write scope of the current thread, see {@link #withDeferredWrites(Supplier)}. */
    private static final ThreadLocal<DeferredWrites> DEFERRED_WRITES = new ThreadLocal<>();

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

    /**
     * When true (and {@link #isAvailable()}), NER named entities / occurrence→model links are dual-written.
     * Default false. Requires models (and typically SimpleText) already available for a meaningful resume.
     */
    @Configurable
    private boolean persistNerConnection = false;

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
     * @return true if Neo4j persistence is available and NER connection dual-write is enabled
     */
    public static boolean shouldPersistNerConnection() {
        return isAvailable() && persistNerConnectionStatic;
    }

    public static void setFailOnPersistenceError(boolean failOnPersistenceError) {
        failOnPersistenceErrorStatic = failOnPersistenceError;
    }

    public static boolean shouldFailOnPersistenceError() {
        return failOnPersistenceErrorStatic;
    }

    /**
     * Runs a persistence side-effect. On {@link RuntimeException} (e.g. Neo4j down), logs and continues.
     * Does not catch {@link Error}.
     */
    public static void runQuietly(String operation, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            if (failOnPersistenceErrorStatic) {
                throw ex;
            }
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
            if (failOnPersistenceErrorStatic) {
                throw ex;
            }
            LOGGER.warn("Persistence operation '{}' failed; using fallback. Cause: {}", operation, ex.toString());
            LOGGER.debug("Persistence failure details for '{}'", operation, ex);
            return fallback;
        }
    }

    /**
     * Saves a noun mapping if TextState persistence is enabled.
     * <p>
     * The save always runs immediately, also inside {@link #withDeferredWrites}: deferred recommended-instance saves of the same scope must find
     * the noun mapping node when they run. Within a scope, a pending deletion of the same {@code ardocoId} is cancelled (the node is updated
     * instead of deleted and recreated).
     *
     * @param nounMapping the noun mapping to save
     */
    public static void saveNounMapping(NounMapping nounMapping) {
        if (!shouldPersistTextState()) {
            return;
        }
        runQuietly("saveNounMapping", () -> getHandler().saveNounMapping(nounMapping));
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.nounMappingSaved(nounMapping.getArdocoId());
        }
    }

    /**
     * Deletes a noun mapping if TextState persistence is enabled. Inside {@link #withDeferredWrites} the deletion is deferred to the end of the
     * scope; it is cancelled if a noun mapping with the same {@code ardocoId} is saved afterwards within the scope.
     *
     * @param ardocoId the ardocoId of the noun mapping to delete
     */
    public static void deleteNounMapping(String ardocoId) {
        if (!shouldPersistTextState()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferNounMappingDeletion(ardocoId);
            return;
        }
        runQuietly("deleteNounMapping", () -> getHandler().deleteNounMapping(ardocoId));
    }

    /**
     * Saves a recommended instance if recommendation persistence is enabled and the metamodel is known. Inside {@link #withDeferredWrites} the save
     * is deferred to the end of the scope; multiple saves of the same instance within a scope result in a single save of its final state.
     *
     * @param recommendedInstance the recommended instance to save
     * @param metamodel           the metamodel of the recommendation state the instance belongs to
     */
    public static void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel) {
        if (metamodel == null || !shouldPersistRecommendations()) {
            return;
        }
        Runnable save = () -> getHandler().saveRecommendedInstance(recommendedInstance, metamodel);
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferSave(recommendedInstance.getId() + "|" + metamodel.name(), "saveRecommendedInstance", save);
            return;
        }
        runQuietly("saveRecommendedInstance", save);
    }

    /**
     * Runs {@code body} as one write scope (used for noun-mapping merges).
     * <p>
     * Within the scope, noun mappings are saved immediately, while recommended-instance saves and noun-mapping deletions are collected. When the
     * outermost scope ends, the collected saves run first (one per instance, with its final state) and the deletions last; a deletion is cancelled
     * if the same {@code ardocoId} is saved after it within the scope. For a merge this yields the order: merged noun mapping upserted, referencing
     * recommended instances re-linked, obsolete noun mapping removed, so the graph is consistent at every point. Nested calls join the outer
     * scope. The collected writes are also flushed if {@code body} throws, so Neo4j mirrors the in-memory state that was actually reached.
     *
     * @param body the code to run
     * @param <T>  the result type
     * @return the result of {@code body}
     */
    public static <T> T withDeferredWrites(Supplier<T> body) {
        if (DEFERRED_WRITES.get() != null) {
            return body.get();
        }
        DeferredWrites scope = new DeferredWrites();
        DEFERRED_WRITES.set(scope);
        T result;
        try {
            result = body.get();
        } catch (RuntimeException | Error ex) {
            DEFERRED_WRITES.remove();
            try {
                scope.flush();
            } catch (RuntimeException flushException) {
                ex.addSuppressed(flushException);
            }
            throw ex;
        }
        DEFERRED_WRITES.remove();
        scope.flush();
        return result;
    }

    /**
     * Writes collected by {@link #withDeferredWrites}. Thread-confined via {@link #DEFERRED_WRITES}.
     */
    private static final class DeferredWrites {
        private final Map<String, DeferredSave> pendingSaves = new LinkedHashMap<>();
        private final Set<String> pendingNounMappingDeletions = new LinkedHashSet<>();

        void deferSave(String key, String operation, Runnable action) {
            this.pendingSaves.put(key, new DeferredSave(operation, action));
        }

        void nounMappingSaved(String ardocoId) {
            // The node is (re)written under this id after the deletion was requested: keep it.
            this.pendingNounMappingDeletions.remove(ardocoId);
        }

        void deferNounMappingDeletion(String ardocoId) {
            this.pendingNounMappingDeletions.add(ardocoId);
        }

        void flush() {
            for (DeferredSave save : this.pendingSaves.values()) {
                runQuietly(save.operation(), save.action());
            }
            for (String ardocoId : this.pendingNounMappingDeletions) {
                runQuietly("deleteNounMapping", () -> getHandler().deleteNounMapping(ardocoId));
            }
        }
    }

    private record DeferredSave(String operation, Runnable action) {
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
        persistNerConnectionStatic = this.persistNerConnection;
    }
}
