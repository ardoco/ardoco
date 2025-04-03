/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators;

import java.io.Serializable;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;

public abstract class Extractor implements Serializable {
    protected String path;

    protected Extractor(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public final Model extractModel(String path) {
        this.path = path;
        return extractModel();
    }

    public abstract Model extractModel();

    public String getModelId() {
        return getModelType().getModelId();
    }

    public abstract ModelType getModelType();
}
