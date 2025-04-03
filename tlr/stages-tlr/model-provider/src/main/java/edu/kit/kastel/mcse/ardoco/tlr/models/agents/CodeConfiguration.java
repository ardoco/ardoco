/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.agents;

import java.io.File;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.Extractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.AllLanguagesExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.CodeExtractor;

public record CodeConfiguration(File code, CodeConfigurationType type) {

    public CodeConfiguration {
        if (code == null || type == null) {
            throw new IllegalArgumentException("Code file and type must not be null");
        }

        if (!code.exists()) {
            throw new IllegalArgumentException("Code file must exist");
        }

        if (type == CodeConfigurationType.DIRECTORY && code.isFile()) {
            throw new IllegalArgumentException("Code file must be a directory in DIRECTORY mode");
        }

        if (type == CodeConfigurationType.ACM_FILE && !code.isFile()) {
            throw new IllegalArgumentException("Code file must be a file in ACM_FILE mode");
        }
    }

    public List<Extractor> extractors() {
        if (type == CodeConfigurationType.DIRECTORY) {
            CodeItemRepository codeItemRepository = new CodeItemRepository();
            CodeExtractor codeExtractor = new AllLanguagesExtractor(codeItemRepository, code.getAbsolutePath());
            return List.of(codeExtractor);
        }
        throw new IllegalStateException("CodeConfigurationType not supported");
    }

    public enum CodeConfigurationType {
        DIRECTORY, ACM_FILE
    }
}
