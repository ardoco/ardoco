/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
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
 * <p>{@link #storage} is the only persistence switch, evaluated once per {@link #applyConfiguration}:
 * <ul>
 * <li>{@code storage = memory} (the default, also when the key is absent): nothing is written to Neo4j; the
 * {@code DataRepository} is the only store.</li>
 * <li>{@code storage = neo4j}: the {@code DataRepository} stays the working store and every persisted kind is written to
 * Neo4j alongside it. A {@link PersistenceHandler} is bootstrapped via {@link ServiceLoader} if none is registered yet;
 * if that is not possible, the configuration fails. Every persistence failure propagates, so the run aborts.</li>
 * </ul>
 * The configuration keys of the removed per-kind write flags ({@link #LEGACY_KEYS}) are rejected with an
 * {@link IllegalArgumentException} naming the storage key.
 */
public class PersistenceBridge extends AbstractConfigurable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceBridge.class);

    private static final PersistenceBridge INSTANCE = new PersistenceBridge();

    /** Default Bolt URI. {@code 127.0.0.1}, not {@code localhost}: avoids IPv6 resolution trouble seen on Windows. */
    private static final String DEFAULT_NEO4J_URI = "bolt://127.0.0.1:7687";
    private static final String DEFAULT_NEO4J_USERNAME = "neo4j";
    private static final String DEFAULT_NEO4J_PASSWORD = "password";

    private static final String KEY_STORAGE = "PersistenceBridge::storage";
    private static final String KEY_NEO4J_URI = "PersistenceBridge::neo4jUri";
    private static final String KEY_NEO4J_USERNAME = "PersistenceBridge::neo4jUsername";
    private static final String KEY_NEO4J_PASSWORD = "PersistenceBridge::neo4jPassword";

    static final List<String> LEGACY_KEYS = List.of("PersistenceBridge::usePersistence", "PersistenceBridge::persistTextState",
            "PersistenceBridge::persistRecommendations", "PersistenceBridge::persistNerConnection");

    /** Max items per batched Cypher round trip (see {@link #chunk(List)}); one commit per chunk is fine, a later failure wipes everything anyway. */
    private static final int WRITE_CHUNK_SIZE = 2000;

    private static PersistenceHandler handler;

    private static String bootstrappedConnection;

    private static PersistenceStorage storageStatic = PersistenceStorage.MEMORY;

    /** Resolved Neo4j URI of the last {@link #applyConfiguration}, for diagnostics; never the password. */
    public static String neo4jUriStatic = DEFAULT_NEO4J_URI;

    /** Resolved Neo4j username of the last {@link #applyConfiguration}, for diagnostics; never the password. */
    public static String neo4jUsernameStatic = DEFAULT_NEO4J_USERNAME;

    /** Write scope of the current thread, see {@link #withDeferredWrites(Supplier)}. */
    private static final ThreadLocal<DeferredWrites> DEFERRED_WRITES = new ThreadLocal<>();

    /** Storage mode for this run, see the class javadoc. */
    @Configurable
    private PersistenceStorage storage = PersistenceStorage.MEMORY;

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
        bootstrappedConnection = null;
    }

    public static void clearHandlerIfRegistered(PersistenceHandler registered) {
        if (handler == registered) {
            setHandler(null);
        }
    }

    public static PersistenceStorage getStorage() {
        return storageStatic;
    }

    public static boolean isNeo4jStorage() {
        return storageStatic == PersistenceStorage.NEO4J;
    }

    public static PersistenceHandler getHandler() {
        if (!isNeo4jStorage()) {
            return null;
        }
        if (handler == null) {
            throw new IllegalStateException(KEY_STORAGE + "=neo4j but no PersistenceHandler is registered");
        }
        return handler;
    }

    /**
     * Saves a noun mapping in Neo4j storage mode. Inside {@link #withDeferredWrites} the save is deferred and batched with the other
     * noun-mapping saves of the scope; a pending deletion of the same {@code ardocoId} is cancelled (the node is updated instead of deleted and
     * recreated).
     *
     * @param nounMapping the noun mapping to save
     */
    public static void saveNounMapping(NounMapping nounMapping) {
        if (!isNeo4jStorage()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferNounMappingSave(nounMapping);
            return;
        }
        getHandler().saveNounMapping(nounMapping);
    }

    /**
     * Saves a collection of trace links in Neo4j storage mode. Inside {@link #withDeferredWrites} the save is deferred and batched with the
     * other trace-link saves of the scope.
     *
     * @param traceLinks the trace links to save
     */
    public static void saveTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
        if (traceLinks.isEmpty() || !isNeo4jStorage()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferTraceLinks(traceLinks);
            return;
        }
        getHandler().saveTraceLinks(traceLinks);
    }

    /**
     * Saves a collection of inconsistencies in Neo4j storage mode. Inside {@link #withDeferredWrites} the save is deferred and batched with
     * the other inconsistency saves of the scope.
     *
     * @param inconsistencies the inconsistencies to save
     */
    public static void addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
        if (inconsistencies.isEmpty() || !isNeo4jStorage()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferInconsistencies(inconsistencies);
            return;
        }
        getHandler().addInconsistencies(inconsistencies);
    }

    /**
     * Saves a named architecture entity in Neo4j storage mode. Inside {@link #withDeferredWrites} the save is deferred and batched (per
     * metamodel and linked/unlinked bucket) with the other NER saves of the scope.
     *
     * @param entity    the entity to save
     * @param metamodel the metamodel bucket this entity belongs to
     * @param unlinked  true if stored in the unlinked set
     */
    public static void saveNamedArchitectureEntity(NamedArchitectureEntity entity, Metamodel metamodel, boolean unlinked) {
        if (!isNeo4jStorage()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferNamedArchitectureEntity(entity, metamodel, unlinked);
            return;
        }
        getHandler().saveNamedArchitectureEntity(entity, metamodel, unlinked);
    }

    /**
     * Deletes a noun mapping in Neo4j storage mode. Inside {@link #withDeferredWrites} the deletion is deferred to the end of the scope; it is
     * cancelled if a noun mapping with the same {@code ardocoId} is saved afterwards within the scope.
     *
     * @param ardocoId the ardocoId of the noun mapping to delete
     */
    public static void deleteNounMapping(String ardocoId) {
        if (!isNeo4jStorage()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferNounMappingDeletion(ardocoId);
            return;
        }
        getHandler().deleteNounMapping(ardocoId);
    }

    /**
     * Saves a recommended instance in Neo4j storage mode if the metamodel is known. Inside {@link #withDeferredWrites} the save is deferred and
     * batched (per metamodel) with the other recommended-instance saves of the scope; multiple saves of the same instance within a scope result
     * in a single save of its final state.
     *
     * @param recommendedInstance the recommended instance to save
     * @param metamodel           the metamodel of the recommendation state the instance belongs to
     */
    public static void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel) {
        if (metamodel == null || !isNeo4jStorage()) {
            return;
        }
        DeferredWrites scope = DEFERRED_WRITES.get();
        if (scope != null) {
            scope.deferRecommendedInstanceSave(recommendedInstance, metamodel);
            return;
        }
        getHandler().saveRecommendedInstance(recommendedInstance, metamodel);
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
     * Runs {@code body} as one write-behind scope: Neo4j writes made during {@code body} (noun mappings, recommended instances, NER entities, trace
     * links, inconsistencies, and noun-mapping deletions) are collected instead of written immediately. When the outermost scope ends successfully,
     * the collected writes are flushed in a fixed order - noun mappings, recommended instances, NER entities, trace links, inconsistencies, then
     * deletions last - because trace links and inconsistencies match their endpoints (noun mappings, recommended instances, NER entities) by id, so
     * those endpoints must already exist in the database. A deletion is cancelled if the same {@code ardocoId} is saved again later in the scope.
     * Nested calls (e.g. a per-stage scope wrapping several per-merge scopes) join the outer scope and do not flush on their own.
     * <p>
     * On failure (an exception from {@code body}), the collected writes are discarded, not flushed: the caller's failure already aborts the whole
     * run, which wipes the database regardless (see the run lifecycle), so flushing a partial, possibly inconsistent batch first would be wasted
     * work and risk. If a flush itself fails, its exception propagates and the remaining writes of the scope are not flushed.
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
            flushNounMappingSaves();
            flushRecommendedInstanceSaves();
            flushNamedArchitectureEntitySaves();
            flushTraceLinks();
            flushInconsistencies();
            for (List<String> batch : chunk(new ArrayList<>(this.pendingNounMappingDeletions))) {
                getHandler().deleteNounMappings(batch);
            }
            logFlush();
        }

        private void flushNounMappingSaves() {
            for (List<NounMapping> batch : chunk(new ArrayList<>(this.pendingNounMappingSaves.values()))) {
                getHandler().saveNounMappings(batch);
            }
        }

        private void flushRecommendedInstanceSaves() {
            Map<Metamodel, List<RecommendedInstance>> byMetamodel = new LinkedHashMap<>();
            for (RecommendedInstanceSave save : this.pendingRecommendedInstanceSaves.values()) {
                byMetamodel.computeIfAbsent(save.metamodel(), ignored -> new ArrayList<>()).add(save.recommendedInstance());
            }
            for (Map.Entry<Metamodel, List<RecommendedInstance>> entry : byMetamodel.entrySet()) {
                for (List<RecommendedInstance> batch : chunk(entry.getValue())) {
                    getHandler().saveRecommendedInstances(batch, entry.getKey());
                }
            }
        }

        private void flushNamedArchitectureEntitySaves() {
            Map<String, List<NamedArchitectureEntity>> byMetamodelAndLinkage = new LinkedHashMap<>();
            Map<String, Metamodel> metamodelByKey = new LinkedHashMap<>();
            Map<String, Boolean> unlinkedByKey = new LinkedHashMap<>();
            for (NamedArchitectureEntitySave save : this.pendingNamedArchitectureEntitySaves.values()) {
                String bucketKey = save.metamodel().name() + "|" + save.unlinked();
                byMetamodelAndLinkage.computeIfAbsent(bucketKey, ignored -> new ArrayList<>()).add(save.entity());
                metamodelByKey.put(bucketKey, save.metamodel());
                unlinkedByKey.put(bucketKey, save.unlinked());
            }
            for (Map.Entry<String, List<NamedArchitectureEntity>> entry : byMetamodelAndLinkage.entrySet()) {
                Metamodel metamodel = metamodelByKey.get(entry.getKey());
                boolean unlinked = unlinkedByKey.get(entry.getKey());
                for (List<NamedArchitectureEntity> batch : chunk(entry.getValue())) {
                    getHandler().saveNamedArchitectureEntities(batch, metamodel, unlinked);
                }
            }
        }

        private void flushTraceLinks() {
            if (!this.pendingTraceLinks.isEmpty()) {
                getHandler().saveTraceLinks(this.pendingTraceLinks);
            }
        }

        private void flushInconsistencies() {
            for (List<Inconsistency> batch : chunk(this.pendingInconsistencies)) {
                getHandler().addInconsistencies(batch);
            }
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
        storageStatic = PersistenceStorage.MEMORY;
        for (String legacyKey : LEGACY_KEYS) {
            if (additionalConfiguration.containsKey(legacyKey)) {
                throw new IllegalArgumentException(legacyKey + " is no longer supported; use " + KEY_STORAGE + "=memory|neo4j instead");
            }
        }
        if (!additionalConfiguration.containsKey(KEY_STORAGE)) {
            this.storage = PersistenceStorage.MEMORY;
        }
        this.neo4jUri = resolveConnectionProperty(additionalConfiguration, KEY_NEO4J_URI, "NEO4J_URI", DEFAULT_NEO4J_URI);
        this.neo4jUsername = resolveConnectionProperty(additionalConfiguration, KEY_NEO4J_USERNAME, "NEO4J_USERNAME", DEFAULT_NEO4J_USERNAME);
        this.neo4jPassword = resolveConnectionProperty(additionalConfiguration, KEY_NEO4J_PASSWORD, "NEO4J_PASSWORD", DEFAULT_NEO4J_PASSWORD);
        neo4jUriStatic = this.neo4jUri;
        neo4jUsernameStatic = this.neo4jUsername;

        if (this.storage == PersistenceStorage.NEO4J) {
            String connection = connectionKey(this.neo4jUri, this.neo4jUsername, this.neo4jPassword);
            if (handler == null || (bootstrappedConnection != null && !bootstrappedConnection.equals(connection))) {
                bootstrapHandler(this.neo4jUri, this.neo4jUsername, this.neo4jPassword);
                bootstrappedConnection = connection;
            }
        }
        storageStatic = this.storage;
    }

    private static String connectionKey(String uri, String username, String password) {
        try {
            byte[] passwordDigest = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            return uri + "\n" + username + "\n" + HexFormat.of().formatHex(passwordDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
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
     * Looks up a {@link PersistenceHandlerProvider} via {@link ServiceLoader} and registers the handler it creates, after the provider's
     * connectivity check. Called when no handler is registered yet, or when the handler was bootstrapped here for other connection settings (a
     * handler set by a test or the Spring activator wins). On any failure no handler stays registered, so the next configuration bootstraps again.
     *
     * @throws IllegalStateException if no provider is registered, or the provider could not connect
     */
    private static void bootstrapHandler(String uri, String username, String password) {
        try {
            createAndRegisterHandler(uri, username, password);
        } catch (RuntimeException ex) {
            setHandler(null);
            throw ex;
        }
    }

    private static void createAndRegisterHandler(String uri, String username, String password) {
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
