/* Licensed under MIT 2024-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.entity;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;

public abstract non-sealed class ArchitectureEntity extends ModelEntity {
    private static final long serialVersionUID = 5118724938904048363L;

    protected ArchitectureEntity(String name, String type) {
        super(name);
    }

    protected ArchitectureEntity(String name, String type, String id) {
        super(name, id);
    }

    public abstract String getType();

    public abstract ImmutableList<String> getNameParts();

    public abstract ImmutableList<String> getTypeParts();

    protected MutableList<String> splitIdentifierIntoParts(String identifier) {
        String splitName = CommonUtilities.splitCases(identifier);
        var names = Lists.mutable.with(splitName.split(" "));
        if (names.size() > 1) {
            names.add(identifier);
        }
        return names;
    }
}
