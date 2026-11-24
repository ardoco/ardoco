/* Licensed under MIT 2024-2026. */
package edu.kit.kastel.mcse.ardoco.core.api.entity;

import java.io.Serial;

import edu.kit.kastel.mcse.ardoco.core.common.IdentifierProvider;

/**
 * Abstract base class for entities that are part of the architecture model.
 */
public abstract non-sealed class ArchitectureEntity extends ModelEntity {

    @Serial
    private static final long serialVersionUID = 5118724938904048363L;

    /**
     * Creates a new architecture entity with the specified name.
     *
     * @param name the name of the architecture entity
     */
    protected ArchitectureEntity(String name) {
        super(name, IdentifierProvider.createId("aam"));
    }

    /**
     * Creates a new architecture entity with the specified name and id.
     *
     * @param name the name of the architecture entity
     * @param id   the unique identifier
     */
    protected ArchitectureEntity(String name, String id) {
        super(name, id);
    }

}
