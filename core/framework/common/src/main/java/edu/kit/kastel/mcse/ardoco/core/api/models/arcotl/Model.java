/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.IdentifierProvider;

public abstract sealed class Model permits ArchitectureModel, CodeModel {

    private final String id = IdentifierProvider.createId();

    public String getId() {
        return this.id;
    }

    /**
     * Returns the content of this model. Contains all elements of the model.
     *
     * @return the content of this model
     */
    public abstract List<? extends Entity> getContent();

    /**
     * Returns the endpoints of this model. Contains all targetable elements for trace links, e.g. compilation units in case of CodeModel.
     *
     * @return the endpoints of this model
     */
    public abstract List<? extends Entity> getEndpoints();

    public abstract Metamodel getMetamodel();
}
