/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.entity;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

public abstract non-sealed class ArchitectureEntity extends ModelEntity {
    private static final long serialVersionUID = 5118724938904048363L;

    protected ArchitectureEntity(String name) {
        super(name);
    }

    protected ArchitectureEntity(String name, String id) {
        super(name, id);
    }

    public abstract Optional<ImmutableList<String>> getNameParts();

    public abstract Optional<String> getType();

    public abstract Optional<ImmutableList<String>> getTypeParts();

}
