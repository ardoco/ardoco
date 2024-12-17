/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.agents;

import java.io.File;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.Extractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.pcm.PcmExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.raw.RawArchitectureExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.UmlExtractor;

public record ArchitectureConfiguration(File architectureFile, ArchitectureModelType type) {
    public ArchitectureConfiguration {
        if (architectureFile == null || type == null) {
            throw new IllegalArgumentException("Architecture file and type must not be null");
        }
        if (!architectureFile.exists() || !architectureFile.isFile()) {
            throw new IllegalArgumentException("Architecture file must exist and be a file");
        }
    }

    public Extractor extractor() {
        return switch (type) {
        case PCM -> new PcmExtractor(architectureFile.getAbsolutePath());
        case UML -> new UmlExtractor(architectureFile.getAbsolutePath());
        case RAW -> new RawArchitectureExtractor(architectureFile.getAbsolutePath());
        };
    }
}
