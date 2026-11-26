/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.AntlrExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaModelMapper;

/**
 * Sets up the ElementExtractor and ModelMapper for the Java language.
 */
public class JavaExtractor extends AntlrExtractor {

    public JavaExtractor(CodeItemRepository repository, String path) {
        this(repository, path, new JavaElementStorageRegistry());
    }

    private JavaExtractor(CodeItemRepository repository, String path, JavaElementStorageRegistry elementManager) {
        super(repository, path, ProgrammingLanguage.JAVA, new JavaModelMapper(repository, elementManager), new JavaElementExtractor(elementManager));
    }
}
