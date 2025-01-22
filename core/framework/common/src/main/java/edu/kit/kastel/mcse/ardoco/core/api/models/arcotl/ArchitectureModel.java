/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;

/**
 * An architecture model that is an AMTL instance.
 */
public final class ArchitectureModel extends Model {

    private static final Logger logger = LoggerFactory.getLogger(ArchitectureModel.class);

    private final List<ArchitectureItem> content;

    /**
     * Creates a new architecture model that is an AMTL instance. The model has the specified architecture items as content.
     *
     * @param content the content of the architecture model
     */
    public ArchitectureModel(List<ArchitectureItem> content) {
        this.content = content;
    }

    @Override
    public List<ArchitectureItem> getContent() {
        return this.content;
    }

    @Override
    public List<ArchitectureItem> getEndpoints() {
        return this.getContent();
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.ARCHITECTURE;
    }

    @Override
    public SortedSet<String> getTypeIdentifiers() {

        SortedSet<String> identifiers = new TreeSet<>();

        for (var entity : getContent()) {
            if (entity.getType().isPresent()) {
                identifiers.add(entity.getType().orElseThrow());
                identifiers.addAll(entity.getTypeParts().orElseThrow().toList());
            }
        }
        return identifiers;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArchitectureModel that) || !super.equals(o)) {
            return false;
        }
        return this.content.equals(that.content);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return 31 * result + this.content.hashCode();
    }

}
