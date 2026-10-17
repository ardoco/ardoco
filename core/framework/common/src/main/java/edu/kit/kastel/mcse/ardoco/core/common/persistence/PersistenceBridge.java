/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.core.configuration.AbstractConfigurable;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;

/**
 * Holder for a concrete {@link PersistenceHandler} (Neo4j) so pipeline stages can access persistence statically.
 *
 * <p>Persistence is optional: {@link #runQuietly} / {@link #callQuietly} isolate Neo4j failures so a down
 * database does not abort the pipeline. Dual-write is transitional; Neo4j sole store is the end vision.
 *
 * <h2>{@code storage} vs. the individual write flags</h2>
 * {@link #storage} is the single switch for "how is this run persisted":
 * <ul>
 * <li>{@code storage = neo4j} unconditionally forces {@link #usePersistence}, {@link #persistTextState},
 * {@link #persistRecommendations} and {@link #persistNerConnection} to {@code true}, and makes persistence failures
 * abort the run (see {@link #shouldFailOnPersistenceError()}). It also bootstraps a {@link PersistenceHandler} via
 * {@link ServiceLoader} if none is registered yet.</li>
 * <li>{@code storage = memory} (the default) leaves the individual flags exactly as configured. Setting
 * {@link #usePersistence} (or one of the other flags) to {@code true} without {@code storage = neo4j} still enables
 * dual-write if a handler happens to be registered (e.g. by a test); this is surprising for normal use and logged as
 * a warning. The individual flags exist mainly for tests and fine-grained measurements; prefer {@code storage} for
 * everything else.</li>
 * </ul>
 * All flags (and the resolved Neo4j connection properties) are reset to their defaults on every
 * {@link #applyConfiguration}: a key absent from the configuration map never keeps the value from a previous run.
 */
public class PersistenceBridge extends AbstractConfigurable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceBridge.class);

    private static final PersistenceBridge INSTANCE = new PersistenceBridge();

    /** Default Bolt URI. {@code 127.0.0.1}, not {@code localhost}: avoids IPv6 resolution trouble seen on Windows. */
    private static final String DEFAULT_NEO4J_URI = "bolt://127.0.0.1:7687";
    private static final String DEFAULT_NEO4J_USERNAME = "neo4j";
    private static final String DEFAULT_NEO4J_PASSWORD = "password";

    private static final String KEY_STORAGE = "PersistenceBridge::storage";
    private static final String KEY_USE_PERSISTENCE = "PersistenceBridge::usePersistence";
    private static final String KEY_PERSIST_TEXT_STATE = "PersistenceBridge::persistTextState";
    private static final String KEY_PERSIST_RECOMMENDATIONS = "PersistenceBridge::persistRecommendations";
    private static final String KEY_PERSIST_NER_CONNECTION = "PersistenceBridge::persistNerConnection";
    private static final String KEY_NEO4J_URI = "PersistenceBridge::neo4jUri";
    private static final String KEY_NEO4J_USERNAME = "PersistenceBridge::neo4jUsername";
    private static final String KEY_NEO4J_PASSWORD = "PersistenceBridge::neo4jPassword";

    /** Max items per batched Cypher round trip (see {@link #chunk(List, int)}); one commit per chunk is fine, a later failure wipes everything anyway. */
    private static final int WRITE_CHUNK_SIZE = 2000;

    private static PersistenceHandler handler;

    public static boolean usePersistenceStatic = false;

    public static boolean persistTextStateStatic = false;

    public static boolean persistRecommendationsStatic = false;

    public static boolean persistNerConnectionStatic = false;

    public static PersistenceStorage storageStatic = PersistenceStorage.MEMORY;

    /** Resolved Neo4j URI of the last {@link #applyConfiguration}, for diagnostics; never the password. */
    public static String neo4jUriStatic = DEFAULT_NEO4J_URI;

    /** Resolved Neo4j username of the last {@link #applyConfiguration}, for diagnostics; never the password. */
    public static String neo4jUsernameStatic = DEFAULT_NEO4J_USERNAME;

    /**
     * Explicit fail-on-persistence-error override, set only via {@link #setFailOnPersistenceError}. Not touched by
     * {@link #applyConfiguration}, so a test (or caller) that sets it before configuring the bridge is not overridden
     * by the reset. See {@link #shouldFailOnPersistenceError()} for how this combines with {@link #storage}.
     */
    private static boolean failOnPersistenceErrorExplicit = false;

    /**
     * Derived fail-on-persistence-error value, recomputed on every {@link #applyConfiguration} as
     * {@code storage == NEO4J}. Unlike the explicit override, this never leaks into the next run: it is recomputed,
     * not merely defaulted, every time.
     */
    private static boolean failOnPersistenceErrorDerived = false;

    /** Write scope of the current thread, see {@link #withDeferredWrites(Supplier)}. */
    private static final ThreadLocal<DeferredWrites> DEFERRED_WRITES = new ThreadLocal<>();

    /**
     * Storage mode for this run. {@code memory} (default) leaves the write flags below as configured;
     * {@code neo4j} forces all of them on. See the class javadoc.
     */
    @Configurable
    private PersistenceStorage storage = PersistenceStorage.MEMORY;

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

    /** Neo4j Bolt URI; resolved from the configuration, then environment, then {@link #DEFAULT_NEO4J_URI}. Not reflectively configurable (no String support in {@link AbstractConfigurable}), read directly from the raw map in {@link #delegateApplyConfigurationToInternalObjects}. */
    private String neo4jUri;

    private String neo4jUsername;

    private String neo4jPassword;

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

    /**
     * Sets the explicit fail-on-persistence-error override (e.g. from a test's {@code @BeforeEach}, before
     * {@code applyConfiguration} runs). Independent of {@link #storage}; see {@link #shouldFailOnPersistenceError()}.
     */
    public static void setFailOnPersistenceError(boolean failOnPersistenceError) {
        failOnPersistenceErrorExplicit = failOnPersistenceError;
    }

    /**
     * {@return whether a persistence failure should abort the run}, i.e. the explicit override
     * ({@link #setFailOnPersistenceError}) or the derived value ({@code storage == NEO4J}, recomputed on every
     * {@link #applyConfiguration}). Neither alone leaks into the next run: the explicit override is set anew by
     * whoever wants it, and the derived value is recomputed, never merely defaulted.
     */
    public static boolean shouldFailOnPersistenceError() {
        return failOnPersistenceErrorExplicit || failOnPersistenceErrorDerived;
    }

    /**
     * Runs a persistence side-effect. On {@link RuntimeException} (e.g. Neo4j down), logs and continues.
     * Does not catch {@link Error}.
     */
    public static void runQuietly(String operation, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            if (shouldFailOnPersistenceError()) {
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
            if (shouldFailOnPersistenceError()) {
                throw ex;
            }
            LOGGER.warn("Persistence operation '{}' failed; using fallback. Cause: {}", operation, ex.toString());
            LOGGER.debug("Persistence failure details for '{}'", operation, ex);
            return fallback;
        }
    }

    /**
     * Saves a noun mapping if TextState persistence is enabled. Inside {@link #withDeferredWrites} the save is deferred and batched with the other
     * noun-mapping saves of the scope; a pending deletion of the same {@code ardocoId} is cancelled (the node is updated instead of deleted and
     * recreated).
     *
     * @param nounMapping the noun mapping to save
     */
    public static void saveNounMapping(NounMapping nounMapping) {
        if (!shouldPersistTextState()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferNounMappingSave(nounMapping);
            return;
        }
        runQuietly("saveNounMapping", () -> getHandler().saveNounMapping(nounMapping));
    }

    /**
     * Saves a collection of trace links if persistence is available. Inside {@link #withDeferredWrites} the save is deferred and batched with the
     * other trace-link saves of the scope.
     *
     * @param traceLinks the trace links to save
     */
    public static void saveTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
        if (traceLinks.isEmpty() || !isAvailable()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferTraceLinks(traceLinks);
            return;
        }
        runQuietly("saveTraceLinks", () -> getHandler().saveTraceLinks(traceLinks));
    }

    /**
     * Saves a collection of inconsistencies if persistence is available. Inside {@link #withDeferredWrites} the save is deferred and batched with
     * the other inconsistency saves of the scope.
     *
     * @param inconsistencies the inconsistencies to save
     */
    public static void addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
        if (inconsistencies.isEmpty() || !isAvailable()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferInconsistencies(inconsistencies);
            return;
        }
        runQuietly("addInconsistencies", () -> getHandler().addInconsistencies(inconsistencies));
    }

    /**
     * Saves a named architecture entity if NER connection persistence is enabled. Inside {@link #withDeferredWrites} the save is deferred and
     * batched (per metamodel and linked/unlinked bucket) with the other NER saves of the scope.
     *
     * @param entity    the entity to save
     * @param metamodel the metamodel bucket this entity belongs to
     * @param unlinked  true if stored in the unlinked set
     */
    public static void saveNamedArchitectureEntity(NamedArchitectureEntity entity, Metamodel metamodel, boolean unlinked) {
        if (!shouldPersistNerConnection()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferNamedArchitectureEntity(entity, metamodel, unlinked);
            return;
        }
        runQuietly("saveNamedArchitectureEntity", () -> getHandler().saveNamedArchitectureEntity(entity, metamodel, unlinked));
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
     * is deferred and batched (per metamodel) with the other recommended-instance saves of the scope; multiple saves of the same instance within a
     * scope result in a single save of its final state.
     *
     * @param recommendedInstance the recommended instance to save
     * @param metamodel           the metamodel of the recommendation state the instance belongs to
     */
    public static void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel) {
        if (metamodel == null || !shouldPersistRecommendations()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferRecommendedInstanceSave(recommendedInstance, metamodel);
            return;
        }
        runQuietly("saveRecommendedInstance", () -> getHandler().saveRecommendedInstance(recommendedInstance, metamodel));
    }

    /**
     * Runs {@code body} as one write-behind scope with no stage name (used for noun-mapping merges, see {@link #withDeferredWrites(String, Supplier)}).
     *
     * @param body the code to run
     * @param <T>  the result type
     * @return the result of {@code body}
     */
    public static <T> T withDeferredWrites(Supplier<T> body) {
        return withDeferredWrites(null, body);
    }

    /**
     * Runs {@code body} as one write-behind scope: dual-writes made during {@code body} (noun mappings, recommended instances, NER entities, trace
     * links, inconsistencies, and noun-mapping deletions) are collected instead of written immediately. When the outermost scope ends successfully,
     * the collected writes are flushed in a fixed order - noun mappings, recommended instances, NER entities, trace links, inconsistencies, then
     * deletions last - because trace links and inconsistencies match their endpoints (noun mappings, recommended instances, NER entities) by id, so
     * those endpoints must already exist in the database. A deletion is cancelled if the same {@code ardocoId} is saved again later in the scope.
     * Nested calls (e.g. a per-stage scope wrapping several per-merge scopes) join the outer scope and do not flush on their own.
     * <p>
     * On failure (an exception from {@code body}), the collected writes are discarded, not flushed: the caller's failure already aborts the whole
     * run, which wipes the database regardless (see the run lifecycle), so flushing a partial, possibly inconsistent batch first would be wasted
     * work and risk. If a flushed batch itself fails (and failures are not configured to throw), the pending noun-mapping deletions are skipped so
     * no existing link is lost.
     *
     * @param stageName the name to log the flush under (e.g. the pipeline step id), or {@code null} for an unnamed scope
     * @param body      the code to run
     * @param <T>       the result type
     * @return the result of {@code body}
     */
    public static <T> T withDeferredWrites(String stageName, Supplier<T> body) {
        if (DEFERRED_WRITES.get() != null) {
            return body.get();
        }
        DeferredWrites scope = new DeferredWrites(stageName);
        DEFERRED_WRITES.set(scope);
        T result;
        try {
            result = body.get();
        } catch (RuntimeException | Error ex) {
            DEFERRED_WRITES.remove();
            throw ex;
        }
        DEFERRED_WRITES.remove();
        scope.flush();
        return result;
    }

    /**
     * Splits {@code items} into chunks of at most {@link #WRITE_CHUNK_SIZE} rows, for batched Cypher round trips.
     */
    private static <T> List<List<T>> chunk(List<T> items) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < items.size(); i += WRITE_CHUNK_SIZE) {
            chunks.add(items.subList(i, Math.min(i + WRITE_CHUNK_SIZE, items.size())));
        }
        return chunks;
    }

    /**
     * Writes collected by {@link #withDeferredWrites}. Thread-confined via {@link #DEFERRED_WRITES}.
     */
    private static final class DeferredWrites {
        private final String stageName;
        private final Map<String, NounMapping> pendingNounMappingSaves = new LinkedHashMap<>();
        private final Map<String, RecommendedInstanceSave> pendingRecommendedInstanceSaves = new LinkedHashMap<>();
        private final Map<String, NamedArchitectureEntitySave> pendingNamedArchitectureEntitySaves = new LinkedHashMap<>();
        private final List<TraceLink<?, ?>> pendingTraceLinks = new ArrayList<>();
        private final List<Inconsistency> pendingInconsistencies = new ArrayList<>();
        private final Set<String> pendingNounMappingDeletions = new LinkedHashSet<>();

        DeferredWrites(String stageName) {
            this.stageName = stageName;
        }

        void deferNounMappingSave(NounMapping nounMapping) {
            this.pendingNounMappingSaves.put(nounMapping.getArdocoId(), nounMapping);
            // The node is (re)written under this id in the same scope: keep it, cancel a pending deletion of it.
            this.pendingNounMappingDeletions.remove(nounMapping.getArdocoId());
        }

        void deferRecommendedInstanceSave(RecommendedInstance recommendedInstance, Metamodel metamodel) {
            String key = recommendedInstance.getId() + "|" + metamodel.name();
            this.pendingRecommendedInstanceSaves.put(key, new RecommendedInstanceSave(recommendedInstance, metamodel));
        }

        void deferNamedArchitectureEntity(NamedArchitectureEntity entity, Metamodel metamodel, boolean unlinked) {
            String key = entity.getId() + "|" + metamodel.name() + "|" + unlinked;
            this.pendingNamedArchitectureEntitySaves.put(key, new NamedArchitectureEntitySave(entity, metamodel, unlinked));
        }

        void deferTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
            this.pendingTraceLinks.addAll(traceLinks);
        }

        void deferInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
            this.pendingInconsistencies.addAll(inconsistencies);
        }

        void deferNounMappingDeletion(String ardocoId) {
            this.pendingNounMappingDeletions.add(ardocoId);
        }

        void flush() {
            int failedBatches = 0;
            failedBatches += flushNounMappingSaves();
            failedBatches += flushRecommendedInstanceSaves();
            failedBatches += flushNamedArchitectureEntitySaves();
            failedBatches += flushTraceLinks();
            failedBatches += flushInconsistencies();

            if (failedBatches > 0 && !this.pendingNounMappingDeletions.isEmpty()) {
                // Deleting now would also remove the old links of the instances that could not be re-linked. Keep the obsolete noun mappings;
                // they are unreferenced in memory and can be cleaned up later, while the graph keeps every link it had.
                LOGGER.warn("{} deferred batch write(s) failed; skipping deletion of {} obsolete noun mapping(s) {} to avoid losing links", failedBatches,
                        this.pendingNounMappingDeletions.size(), this.pendingNounMappingDeletions);
            } else {
                for (String ardocoId : this.pendingNounMappingDeletions) {
                    runQuietly("deleteNounMapping", () -> getHandler().deleteNounMapping(ardocoId));
                }
            }
            logFlush();
        }

        private int flushNounMappingSaves() {
            if (this.pendingNounMappingSaves.isEmpty()) {
                return 0;
            }
            int failed = 0;
            for (List<NounMapping> batch : chunk(new ArrayList<>(this.pendingNounMappingSaves.values()))) {
                boolean[] succeeded = { false };
                runQuietly("saveNounMappings", () -> {
                    getHandler().saveNounMappings(batch);
                    succeeded[0] = true;
                });
                if (!succeeded[0]) {
                    failed++;
                }
            }
            return failed;
        }

        private int flushRecommendedInstanceSaves() {
            if (this.pendingRecommendedInstanceSaves.isEmpty()) {
                return 0;
            }
            Map<Metamodel, List<RecommendedInstance>> byMetamodel = new LinkedHashMap<>();
            for (RecommendedInstanceSave save : this.pendingRecommendedInstanceSaves.values()) {
                byMetamodel.computeIfAbsent(save.metamodel(), ignored -> new ArrayList<>()).add(save.recommendedInstance());
            }
            int failed = 0;
            for (Map.Entry<Metamodel, List<RecommendedInstance>> entry : byMetamodel.entrySet()) {
                Metamodel metamodel = entry.getKey();
                for (List<RecommendedInstance> batch : chunk(entry.getValue())) {
                    boolean[] succeeded = { false };
                    runQuietly("saveRecommendedInstances", () -> {
                        getHandler().saveRecommendedInstances(batch, metamodel);
                        succeeded[0] = true;
                    });
                    if (!succeeded[0]) {
                        failed++;
                    }
                }
            }
            return failed;
        }

        private int flushNamedArchitectureEntitySaves() {
            if (this.pendingNamedArchitectureEntitySaves.isEmpty()) {
                return 0;
            }
            Map<String, List<NamedArchitectureEntity>> byMetamodelAndLinkage = new LinkedHashMap<>();
            Map<String, Metamodel> metamodelByKey = new LinkedHashMap<>();
            Map<String, Boolean> unlinkedByKey = new LinkedHashMap<>();
            for (NamedArchitectureEntitySave save : this.pendingNamedArchitectureEntitySaves.values()) {
                String bucketKey = save.metamodel().name() + "|" + save.unlinked();
                byMetamodelAndLinkage.computeIfAbsent(bucketKey, ignored -> new ArrayList<>()).add(save.entity());
                metamodelByKey.put(bucketKey, save.metamodel());
                unlinkedByKey.put(bucketKey, save.unlinked());
            }
            int failed = 0;
            for (Map.Entry<String, List<NamedArchitectureEntity>> entry : byMetamodelAndLinkage.entrySet()) {
                Metamodel metamodel = metamodelByKey.get(entry.getKey());
                boolean unlinked = unlinkedByKey.get(entry.getKey());
                for (List<NamedArchitectureEntity> batch : chunk(entry.getValue())) {
                    boolean[] succeeded = { false };
                    runQuietly("saveNamedArchitectureEntities", () -> {
                        getHandler().saveNamedArchitectureEntities(batch, metamodel, unlinked);
                        succeeded[0] = true;
                    });
                    if (!succeeded[0]) {
                        failed++;
                    }
                }
            }
            return failed;
        }

        private int flushTraceLinks() {
            if (this.pendingTraceLinks.isEmpty()) {
                return 0;
            }
            int failed = 0;
            for (List<TraceLink<?, ?>> batch : chunk(this.pendingTraceLinks)) {
                boolean[] succeeded = { false };
                runQuietly("saveTraceLinks", () -> {
                    getHandler().saveTraceLinks(batch);
                    succeeded[0] = true;
                });
                if (!succeeded[0]) {
                    failed++;
                }
            }
            return failed;
        }

        private int flushInconsistencies() {
            if (this.pendingInconsistencies.isEmpty()) {
                return 0;
            }
            int failed = 0;
            for (List<Inconsistency> batch : chunk(this.pendingInconsistencies)) {
                boolean[] succeeded = { false };
                runQuietly("addInconsistencies", () -> {
                    getHandler().addInconsistencies(batch);
                    succeeded[0] = true;
                });
                if (!succeeded[0]) {
                    failed++;
                }
            }
            return failed;
        }

        private void logFlush() {
            if (this.pendingNounMappingSaves.isEmpty() && this.pendingRecommendedInstanceSaves.isEmpty()
                    && this.pendingNamedArchitectureEntitySaves.isEmpty() && this.pendingTraceLinks.isEmpty() && this.pendingInconsistencies.isEmpty()
                    && this.pendingNounMappingDeletions.isEmpty()) {
                return;
            }
            StringBuilder counts = new StringBuilder();
            appendCount(counts, "NounMappings", this.pendingNounMappingSaves.size());
            appendCount(counts, "RecommendedInstances", this.pendingRecommendedInstanceSaves.size());
            appendCount(counts, "NamedArchitectureEntities", this.pendingNamedArchitectureEntitySaves.size());
            appendCount(counts, "TraceLinks", this.pendingTraceLinks.size());
            appendCount(counts, "Inconsistencies", this.pendingInconsistencies.size());
            appendCount(counts, "NounMapping deletions", this.pendingNounMappingDeletions.size());
            LOGGER.info("Flushed persistence for stage {}: {}", this.stageName == null ? "<unnamed>" : this.stageName, counts);
        }

        private static void appendCount(StringBuilder builder, String label, int count) {
            if (count == 0) {
                return;
            }
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(count).append(' ').append(label);
        }
    }

    private record RecommendedInstanceSave(RecommendedInstance recommendedInstance, Metamodel metamodel) {
    }

    private record NamedArchitectureEntitySave(NamedArchitectureEntity entity, Metamodel metamodel, boolean unlinked) {
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(ImmutableSortedMap<String, String> additionalConfiguration) {
        // The reflective pass in AbstractConfigurable only overwrites fields whose key is present in
        // additionalConfiguration. Without this reset, a value from an earlier applyConfiguration call on this
        // singleton (e.g. a previous run or test in the same JVM) would leak into a run that never set it.
        if (!additionalConfiguration.containsKey(KEY_STORAGE)) {
            this.storage = PersistenceStorage.MEMORY;
        }
        if (!additionalConfiguration.containsKey(KEY_USE_PERSISTENCE)) {
            this.usePersistence = false;
        }
        if (!additionalConfiguration.containsKey(KEY_PERSIST_TEXT_STATE)) {
            this.persistTextState = false;
        }
        if (!additionalConfiguration.containsKey(KEY_PERSIST_RECOMMENDATIONS)) {
            this.persistRecommendations = false;
        }
        if (!additionalConfiguration.containsKey(KEY_PERSIST_NER_CONNECTION)) {
            this.persistNerConnection = false;
        }
        this.neo4jUri = resolveConnectionProperty(additionalConfiguration, KEY_NEO4J_URI, "NEO4J_URI", DEFAULT_NEO4J_URI);
        this.neo4jUsername = resolveConnectionProperty(additionalConfiguration, KEY_NEO4J_USERNAME, "NEO4J_USERNAME", DEFAULT_NEO4J_USERNAME);
        this.neo4jPassword = resolveConnectionProperty(additionalConfiguration, KEY_NEO4J_PASSWORD, "NEO4J_PASSWORD", DEFAULT_NEO4J_PASSWORD);

        if (this.storage == PersistenceStorage.NEO4J) {
            this.usePersistence = true;
            this.persistTextState = true;
            this.persistRecommendations = true;
            this.persistNerConnection = true;
        } else if (this.usePersistence) {
            LOGGER.warn("PersistenceBridge::usePersistence is enabled without PersistenceBridge::storage=neo4j; dual-write only works if a "
                    + "PersistenceHandler happens to already be registered (e.g. by a test). Prefer storage=neo4j for a real run.");
        }

        if (this.persistRecommendations && !this.persistTextState) {
            LOGGER.info("persistRecommendations is enabled without persistTextState; enabling persistTextState automatically");
            this.persistTextState = true;
        }

        usePersistenceStatic = this.usePersistence;
        persistTextStateStatic = this.persistTextState;
        persistRecommendationsStatic = this.persistRecommendations;
        persistNerConnectionStatic = this.persistNerConnection;
        storageStatic = this.storage;
        neo4jUriStatic = this.neo4jUri;
        neo4jUsernameStatic = this.neo4jUsername;
        failOnPersistenceErrorDerived = this.storage == PersistenceStorage.NEO4J;

        if (this.storage == PersistenceStorage.NEO4J && handler == null) {
            bootstrapHandler(this.neo4jUri, this.neo4jUsername, this.neo4jPassword);
        }
    }

    /**
     * Resolves a connection property: the configuration map, then the environment variable, then the default.
     * {@code neo4jUri}/{@code neo4jUsername}/{@code neo4jPassword} are not {@link Configurable} fields because
     * {@link AbstractConfigurable}'s reflective parsing has no {@link String} branch; they are read directly from the
     * raw map instead.
     */
    private static String resolveConnectionProperty(ImmutableSortedMap<String, String> additionalConfiguration, String key, String envVariable,
            String defaultValue) {
        String configured = additionalConfiguration.get(key);
        if (configured != null) {
            return configured;
        }
        String fromEnv = Environment.getEnv(envVariable);
        return fromEnv != null ? fromEnv : defaultValue;
    }

    /**
     * Looks up a {@link PersistenceHandlerProvider} via {@link ServiceLoader} and registers the handler it creates.
     * Does nothing if a handler is already registered (a test, the Spring activator, or an earlier run in this JVM
     * already set one): that handler wins, matching today's test setup.
     *
     * @throws IllegalStateException if no provider is registered, or the provider could not connect
     */
    private static void bootstrapHandler(String uri, String username, String password) {
        PersistenceHandlerProvider provider = ServiceLoader.load(PersistenceHandlerProvider.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "PersistenceBridge::storage=neo4j but no PersistenceHandlerProvider is registered via ServiceLoader. Add a module "
                                + "providing one (e.g. 'neo4j-schema') to the runtime classpath. Attempted connection: " + uri + " as " + username));

        var connectionProperties = SortedMaps.mutable.<String, String>empty();
        connectionProperties.put(PersistenceHandlerProvider.PROPERTY_URI, uri);
        connectionProperties.put(PersistenceHandlerProvider.PROPERTY_USERNAME, username);
        connectionProperties.put(PersistenceHandlerProvider.PROPERTY_PASSWORD, password);

        PersistenceHandler created;
        try {
            created = provider.createHandler(connectionProperties.toImmutable());
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "PersistenceBridge::storage=neo4j: could not connect to Neo4j at " + uri + " as " + username + ": " + ex.getMessage(), ex);
        }
        if (created == null) {
            throw new IllegalStateException("PersistenceHandlerProvider " + provider.getClass().getName() + " returned no handler for " + uri);
        }
        setHandler(created);
        LOGGER.info("Neo4j persistence bootstrapped via ServiceLoader provider {} (uri={}, username={})", provider.getClass().getName(), uri,
                username);
    }
}
