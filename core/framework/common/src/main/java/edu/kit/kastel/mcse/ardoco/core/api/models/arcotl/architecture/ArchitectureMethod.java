/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture;

import java.util.Optional;

import org.eclipse.collections.api.list.ImmutableList;

// TODO: Discuss how to handle the missing type
public final class ArchitectureMethod extends ArchitectureItem {

    private static final long serialVersionUID = 6560555992331464264L;

    public ArchitectureMethod(String name) {
        super(name);
    }

    @Override
    public Optional<String> getType() {
        // Not implemented yet. Methods currently have no specified type.//
        return Optional.empty();
    }

    @Override
    public Optional<ImmutableList<String>> getTypeParts() {
        // Not implemented yet. Methods currently have no specified type.
        return Optional.empty();
    }

}
