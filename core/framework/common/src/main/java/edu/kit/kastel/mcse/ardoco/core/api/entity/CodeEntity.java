/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.entity;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

public non-sealed class CodeEntity extends ModelEntity {
    private static final long serialVersionUID = 5520572653996476974L;

    protected CodeEntity(String name) {
        super(name);
    }

    protected CodeEntity(String name, String id) {
        super(name, id);
    }

    @Override
    public Optional<String> getType() {
        return Optional.empty();
    }

    @Override
    public Optional<ImmutableList<String>> getTypeParts() {
        if (this.getType().isPresent()) {
            return Optional.of(splitIdentifierIntoParts(this.getType().get()).toImmutable());
        }
        return Optional.empty();
    }

    @Override
    public Optional<ImmutableList<String>> getNameParts() {
        return Optional.empty();
    }

}
