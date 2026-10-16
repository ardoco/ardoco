/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.common.persistence;

/**
 * Storage mode for {@link PersistenceBridge}.
 *
 * @see PersistenceBridge
 */
public enum PersistenceStorage {
    /** No Neo4j dual-write; the {@link edu.kit.kastel.mcse.ardoco.core.data.DataRepository DataRepository} is the only store. Default. */
    MEMORY("memory"),
    /**
     * Neo4j is built up during the run like the DataRepository (text, models, NounMappings, RecommendedInstances, all states), and forces
     * every {@code PersistenceBridge} write flag (incl. NER) on.
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
