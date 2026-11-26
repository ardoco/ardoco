/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.AntlrExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3.Python3ElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.Python3ModelMapper;

/**
 * Sets up the ElementExtractor and ModelMapper for the Python3 language.
 */
public class Python3Extractor extends AntlrExtractor {

    public Python3Extractor(CodeItemRepository repository, String path) {
        this(repository, path, new Python3ElementStorageRegistry());
    }

    private Python3Extractor(CodeItemRepository repository, String path, Python3ElementStorageRegistry elementManager) {
        super(repository, path, ProgrammingLanguage.PYTHON, new Python3ModelMapper(repository, elementManager), new Python3ElementExtractor(elementManager));
    }

}
