/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

// TODO: Discuss how to handle the missing type
public final class ArchitectureMethod extends ArchitectureItem {

    private static final long serialVersionUID = 6560555992331464264L;

    private final MutableList<String> nameParts;

    public ArchitectureMethod(String name) {
        super(name, null);
        this.nameParts = splitIdentifierIntoParts(name);
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not implemented yet. Methods currently have no specified type.");
    }

    @Override
    public ImmutableList<String> getNameParts() {
        return this.nameParts.toImmutable();
    }

    @Override
    public ImmutableList<String> getTypeParts() {
        throw new UnsupportedOperationException("Not implemented yet. Methods currently have no specified type.");
    }

}
