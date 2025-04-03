/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.Extractor;

public abstract class ArchitectureExtractor extends Extractor {

    protected ArchitectureExtractor(String path) {
        super(path);
    }

    @Override
    public abstract ArchitectureModel extractModel();

}
