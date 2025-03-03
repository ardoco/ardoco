/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.entity;

import java.util.Optional;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;

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

    protected ModelEntity(String name, String id) {
        super(name, id);
    }

    public abstract Optional<String> getType();

    public abstract Optional<ImmutableList<String>> getTypeParts();

    public ImmutableList<String> getNameParts() {
        return splitIdentifierIntoParts(this.getName()).toImmutable();
    }

    protected MutableList<String> splitIdentifierIntoParts(String identifier) {
        String splitName = CommonUtilities.splitCases(identifier);
        var names = Lists.mutable.with(splitName.split(" "));
        if (names.size() > 1) {
            names.add(identifier);
        }
        return names;
    }

}
