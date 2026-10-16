/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
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
     * scope. The collected writes are also flushed if {@code body} throws, so Neo4j mirrors the in-memory state that was actually reached. If
     * a collected save fails (and failures are not configured to throw), the deletions are skipped so no existing link is lost.
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
            int failedSaves = 0;
            for (DeferredSave save : this.pendingSaves.values()) {
                boolean[] succeeded = { false };
                runQuietly(save.operation(), () -> {
                    save.action().run();
                    succeeded[0] = true;
                });
                if (!succeeded[0]) {
                    failedSaves++;
                }
            }
            if (failedSaves > 0 && !this.pendingNounMappingDeletions.isEmpty()) {
                // Deleting now would also remove the old links of the instances that could not be re-linked. Keep the obsolete noun mappings;
                // they are unreferenced in memory and can be cleaned up later, while the graph keeps every link it had.
                LOGGER.warn("{} deferred save(s) failed; skipping deletion of {} obsolete noun mapping(s) {} to avoid losing links", failedSaves,
                        this.pendingNounMappingDeletions.size(), this.pendingNounMappingDeletions);
                return;
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
