/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.util;

import java.util.Optional;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;

public class FakeArchitectureEntity extends ArchitectureEntity {
    private final String type;

    public FakeArchitectureEntity(String name, String id, String type) {
        super(name, id);
        this.type = type;
    }

    @Override
    public Optional<String> getType() {
        return Optional.ofNullable(type);
    }

    @Override
    public ImmutableList<String> getTypeParts() {
        if (type == null)
            return Lists.immutable.empty();
        return Lists.immutable.with(type.split(" "));
    }
}
