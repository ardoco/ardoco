/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.generators.java;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.java.JavaExtractor;

class JavaExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(JavaExtractorTest.class);

    @Test
    void extractorTest() {
        var extractor = new JavaExtractor(new CodeItemRepository(), "src/test/resources/interface", Metamodel.CODE_WITH_COMPILATION_UNITS);
        CodeModel model = extractor.extractModel();
        Assertions.assertNotNull(model);
        for (Entity codePackage : model.getAllPackages()) {
            Assertions.assertNotNull(codePackage);
            logger.info("Package: {}", codePackage);
        }

        Assertions.assertEquals(7, model.getEndpoints().size());

        Stream<CodeCompilationUnit> allCompilationUnits = model.getAllPackages().stream().flatMap(p -> p.getCompilationUnits().stream());
        CodeCompilationUnit aClass = allCompilationUnits.filter(u -> "AClass".equals(u.getName())).findFirst().orElseThrow();
        Assertions.assertTrue(aClass.getImportedModuleNames().contains("edu.zwei.OtherInterface"));
    }
}
