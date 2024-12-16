/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture;

import java.util.Objects;
import java.util.SortedSet;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

public final class ArchitectureInterface extends ArchitectureItem {

    private static final long serialVersionUID = 2232013345166120690L;

    private final SortedSet<ArchitectureMethod> signatures;

    private final String type;
    private MutableList<String> nameParts;
    private MutableList<String> typeParts;

    public ArchitectureInterface(String name, String id, SortedSet<ArchitectureMethod> signatures, String type) {
        super(name, id);
        this.signatures = signatures;
        this.type = type;

    }

    public SortedSet<ArchitectureMethod> getSignatures() {
        return this.signatures;
    }

    @Override
    public String toString() {
        return "Interface: " + this.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArchitectureInterface that) || !super.equals(o)) {
            return false;
        }
        return Objects.equals(this.signatures, that.signatures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.signatures);
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public ImmutableList<String> getNameParts() {
        return this.nameParts.toImmutable();
    }

    @Override
    public ImmutableList<String> getTypeParts() {
        return this.typeParts.toImmutable();
    }
}
