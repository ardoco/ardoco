/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

/**
 * Bootstraps a concrete {@link PersistenceHandler} for {@link PersistenceBridge}, discovered via
 * {@link java.util.ServiceLoader} (a {@code META-INF/services} entry).
 *
 * <p>This indirection exists so that runner modules (e.g. {@code pipeline-tlr}, {@code pipeline-id}) never need a
 * compile-time dependency on {@code neo4j-schema}: that module already depends on them (it builds the pipeline
 * states it persists), so a direct dependency the other way would be a cycle. {@code neo4j-schema} instead ships
 * both the implementation and its {@code META-INF/services} registration; {@link PersistenceBridge} finds it purely
 * at runtime.
 *
 * @see PersistenceBridge
 */
public interface PersistenceHandlerProvider {

    /** Connection property key for the Bolt URI, e.g. {@code bolt://127.0.0.1:7687}. */
    String PROPERTY_URI = "uri";

    /** Connection property key for the database username. */
    String PROPERTY_USERNAME = "username";

    /** Connection property key for the database password. Never logged by implementations. */
    String PROPERTY_PASSWORD = "password";

    /**
     * Creates or reuses a {@link PersistenceHandler} connected with the given properties.
     *
     * @param connectionProperties known keys: {@link #PROPERTY_URI}, {@link #PROPERTY_USERNAME}, {@link #PROPERTY_PASSWORD}
     * @return a handler ready to use; never {@code null}
     * @throws RuntimeException if no connection could be established
     */
    PersistenceHandler createHandler(ImmutableSortedMap<String, String> connectionProperties);
}
