/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.core.api.entity;

public abstract non-sealed class ArchitectureEntity extends Entity {
    private static final long serialVersionUID = 5118724938904048363L;

    protected ArchitectureEntity(String name) {
        super(name);
    }

    protected ArchitectureEntity(String name, String id) {
        super(name, id);
    }
}
