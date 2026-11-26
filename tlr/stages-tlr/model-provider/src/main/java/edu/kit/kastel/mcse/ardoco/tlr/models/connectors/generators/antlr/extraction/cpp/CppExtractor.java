/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.AntlrExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppModelMapper;

/**
 * Sets up the ElementExtractor and ModelMapper for the C++ language.
 */
public class CppExtractor extends AntlrExtractor {

    public CppExtractor(CodeItemRepository repository, String path) {
        this(repository, path, new CppElementStorageRegistry());
    }

    private CppExtractor(CodeItemRepository repository, String path, CppElementStorageRegistry elementManager) {
        super(repository, path, ProgrammingLanguage.CPP, new CppModelMapper(repository, elementManager), new CppElementExtractor(elementManager));
    }
}
