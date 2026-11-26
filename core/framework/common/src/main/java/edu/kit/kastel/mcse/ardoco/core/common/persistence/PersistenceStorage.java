/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

/**
 * Storage mode for {@link PersistenceBridge}.
 *
 * @see PersistenceBridge
 */
public enum PersistenceStorage {
    /** No Neo4j writes; the {@link edu.kit.kastel.mcse.ardoco.core.data.DataRepository DataRepository} is the only store. Default. */
    MEMORY("memory"),
    /**
     * Neo4j is built up during the run alongside the DataRepository (text, models, NounMappings, RecommendedInstances, NER, trace links,
     * inconsistencies).
     */
    NEO4J("neo4j");

    private final String key;

    PersistenceStorage(String key) {
        this.key = key;
    }

    /**
     * {@return the lower-case configuration value, e.g. {@code "memory"} or {@code "neo4j"}} Matched exactly (case-sensitively) by
     * {@code AbstractConfigurable}'s enum parsing, consistent with every other configuration value in ArDoCo.
     */
    @Override
    public String toString() {
        return this.key;
    }
}
