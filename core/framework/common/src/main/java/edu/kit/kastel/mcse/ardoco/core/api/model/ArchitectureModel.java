/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.model;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.model.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.architecture.NoHashCodeEquals;

/**
 * Represents an architecture model.
 */
@NoHashCodeEquals
public abstract sealed class ArchitectureModel extends Model permits ArchitectureComponentModel, ArchitectureModelWithComponentsAndInterfaces {

    /**
     * Returns the content of the architecture model.
     *
     * @return list of architecture items
     */
    @Override
    public abstract List<? extends ArchitectureItem> getContent();

    /**
     * Returns the endpoints of this model.
     *
     * @return list of architecture items
     */
    @Override
    public abstract List<? extends ArchitectureItem> getEndpoints();
}
