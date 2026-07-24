/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.mappers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3.Python3Extractor;

class Python3ModelMapperTest {

    @Test
    void testPython3ModelMapper() {
        CodeItemRepository repository = new CodeItemRepository();
        Python3Extractor extractor = new Python3Extractor(repository, "src/test/resources/python/interface/edu/");
        CodeModel codeModel = extractor.extractModel();

        // More Detailed Assertions
        Assertions.assertEquals(3, codeModel.getAllPackages().size());

    }

    @Test
    void importsArePopulatedOnCompilationUnits() {
        CodeItemRepository repository = new CodeItemRepository();
        Python3Extractor extractor = new Python3Extractor(repository, "src/test/resources/python/interface/edu/");
        CodeModel codeModel = extractor.extractModel();

        CodeCompilationUnit abcUnit = allCompilationUnits(codeModel).filter(u -> "APyAbstractBaseClass".equals(u.getName())).findFirst().orElseThrow();
        Assertions.assertTrue(abcUnit.getImportedModuleNames().contains("abc.ABC"));
        Assertions.assertTrue(abcUnit.getImportedModuleNames().contains("abc.abstractmethod"));

        CodeCompilationUnit dataclassUnit = allCompilationUnits(codeModel).filter(u -> "APyDataClass".equals(u.getName())).findFirst().orElseThrow();
        Assertions.assertTrue(dataclassUnit.getImportedModuleNames().contains("dataclasses.dataclass"));
    }

    @Test
    void compilationUnitsHaveParentPackage() {
        CodeItemRepository repository = new CodeItemRepository();
        Python3Extractor extractor = new Python3Extractor(repository, "src/test/resources/python/interface/edu/");
        CodeModel codeModel = extractor.extractModel();

        Assertions.assertFalse(allCompilationUnits(codeModel).findAny().isEmpty());
        Assertions.assertTrue(allCompilationUnits(codeModel).allMatch(u -> u.hasParent()));
    }

    private static Stream<CodeCompilationUnit> allCompilationUnits(CodeModel codeModel) {
        return codeModel.getEndpoints().stream().filter(CodeCompilationUnit.class::isInstance).map(CodeCompilationUnit.class::cast);
    }

}
