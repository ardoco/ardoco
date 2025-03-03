/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.List;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.IdentifierProvider;

public abstract sealed class Model permits ArchitectureModel, ComponentModel, CoarseGrainedCodeModel, CodeModel {

    private final String id = IdentifierProvider.createId();

    public String getId() {
        return this.id;
    }

    /**
     * Returns the content of this model. Contains all elements of the model.
     *
     * @return the content of this model
     */
    public abstract List<? extends ModelEntity> getContent();

    /**
     * Returns the endpoints of this model. Contains all targetable elements for trace links, e.g. compilation units in case of CodeModel.
     *
     * @return the endpoints of this model
     */
    public abstract List<? extends ModelEntity> getEndpoints();

    public abstract Metamodel getMetamodel();

    /**
     * Returns a set of identifiers for the types in the model state.
     *
     * @return Set of identifiers for existing types
     */
    public abstract SortedSet<String> getTypeIdentifiers();

}
