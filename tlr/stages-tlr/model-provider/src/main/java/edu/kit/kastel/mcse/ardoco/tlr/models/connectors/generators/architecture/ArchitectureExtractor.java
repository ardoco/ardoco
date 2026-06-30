/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture;

import static edu.kit.kastel.mcse.ardoco.core.common.JsonHandling.createObjectMapper;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.Extractor;

public abstract class ArchitectureExtractor extends Extractor {

    private static final Logger logger = LoggerFactory.getLogger(ArchitectureExtractor.class);

    private static final String ARCHITECTURE_MODEL_FILE_NAME = "architectureModel.aam";

    protected ArchitectureExtractor(String path, Metamodel metamodelToExtract) {
        super(path, metamodelToExtract);
    }

    @Override
    public abstract ArchitectureModel extractModel();

    public void writeOutArchitectureModel(ArchitectureModel architectureModel, File outputFile) {
        ObjectMapper objectMapper = createObjectMapper();
        try {
            objectMapper.writeValue(outputFile, architectureModel.createArchitectureModelDto());
        } catch (IOException e) {
            logger.warn("An exception occurred when writing the architecture model.", e);
        }
    }

    public void writeOutArchitectureModel(ArchitectureModel architectureModel) {
        File file = new File(getArchitectureModelFileString());
        writeOutArchitectureModel(architectureModel, file);
    }

    private String getArchitectureModelFileString() {
        return path + File.separator + ARCHITECTURE_MODEL_FILE_NAME;
    }
}
