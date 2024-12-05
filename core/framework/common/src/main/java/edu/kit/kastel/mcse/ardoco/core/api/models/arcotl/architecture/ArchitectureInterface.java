/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture;

import java.util.Objects;
import java.util.SortedSet;

public final class ArchitectureInterface extends ArchitectureItem {

    private static final long serialVersionUID = 2232013345166120690L;

    private final SortedSet<ArchitectureMethod> signatures;

    public ArchitectureInterface(String name, String id, SortedSet<ArchitectureMethod> signatures) {
        super(name, id);
        this.signatures = signatures;
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
}
