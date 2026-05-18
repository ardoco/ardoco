/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.inconsistencies;

public enum ArchitectureType {
    ARCHITECTURE, CODE, DOCUMENTATION;

    public boolean isModel() {
        return this == CODE || this == ARCHITECTURE;
    }
}
