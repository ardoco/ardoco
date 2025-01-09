/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.entity;

public abstract sealed class ModelEntity extends Entity permits ArchitectureEntity, CodeEntity {

    protected ModelEntity() {
        // Jackson
        super(null);
    }

    /**
     * Creates a new entity with the specified name.
     *
     * @param name the name of the entity to be created
     */
    protected ModelEntity(String name) {
        super(name);
    }

    protected ModelEntity(String name, String type) {
        super(name, type);
    }
}
