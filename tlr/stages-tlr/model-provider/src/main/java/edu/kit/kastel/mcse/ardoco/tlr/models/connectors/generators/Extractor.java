/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;

public abstract class Extractor {
    protected String path;

    protected Extractor(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }

    public final Model extractModel(String path) {
        this.path = path;
        return this.extractModel();
    }

    public abstract Model extractModel();

    public Metamodel getModelId() {
        return this.getModelType().getMetamodel();
    }

    public abstract ModelType getModelType();
}
