/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;

/**
 * Represents a model containing only architecture components. Provides access to architecture components and their type identifiers.
 */
public final class ArchitectureComponentModel extends ArchitectureModel {

    private final ArchitectureModel architectureModel;

    /**
     * Creates a new ArchitectureComponentModel.
     *
     * @param architectureModel the architecture model
     */
    public ArchitectureComponentModel(ArchitectureModel architectureModel) {
        this.architectureModel = Objects.requireNonNull(architectureModel);
    }

    /**
     * Returns the architecture components in this model.
     *
     * @return list of architecture components
     */
    @Override
    public List<ArchitectureComponent> getContent() {
        List<ArchitectureComponent> components = new ArrayList<>();
        for (ArchitectureItem item : architectureModel.getContent()) {
            if (item instanceof ArchitectureComponent component) {
                components.add(component);
            }
        }
        return components;
    }

    /**
     * Returns the endpoints of this model. In this case, it returns the architecture components as endpoints.
     *
     * @return list of model entities
     */
    @Override
    public List<? extends ArchitectureItem> getEndpoints() {
        return this.getContent();
    }

    /**
     * Returns the metamodel of this model.
     *
     * @return the metamodel
     */
    @Override
    public Metamodel getMetamodel() {
        return Metamodel.ARCHITECTURE_WITH_COMPONENTS;
    }

    /**
     * Returns the type identifiers of the architecture components in this model.
     *
     * @return sorted set of type identifiers
     */
    @Override
    public SortedSet<String> getTypeIdentifiers() {
        SortedSet<String> identifiers = new TreeSet<>();
        for (var component : getContent()) {
            if (component.getType().isPresent()) {
                identifiers.add(component.getType().orElseThrow());
                identifiers.addAll(component.getTypeParts().toList());
            }
        }
        return identifiers;
    }
}
