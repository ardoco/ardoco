/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;

public abstract class Extractor {
    protected final Metamodel metamodelToExtract;
    protected String path;

    protected Extractor(String path, Metamodel metamodelToExtract) {
        this.path = path;
        this.metamodelToExtract = metamodelToExtract;
    }

    public abstract Model extractModel();

    /**
     * Returns the prefix of the ids that are generated while this extractor runs.
     *
     * @return the id prefix, or null to use the default prefix of the created entity kind
     */
    public String getIdPrefix() {
        return null;
    }

    public final Metamodel getMetamodel() {
        return this.metamodelToExtract;
    }
}
