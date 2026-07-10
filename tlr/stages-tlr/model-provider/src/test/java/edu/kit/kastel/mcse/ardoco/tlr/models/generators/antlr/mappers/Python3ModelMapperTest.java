/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.mappers;

import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeAssembly;
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
    void importsArePopulatedOnAssemblies() {
        CodeItemRepository repository = new CodeItemRepository();
        Python3Extractor extractor = new Python3Extractor(repository, "src/test/resources/python/interface/edu/");
        CodeModel codeModel = extractor.extractModel();

        CodeAssembly abcAssembly = allAssemblies(codeModel).filter(a -> "APyAbstractBaseClass".equals(a.getName())).findFirst().orElseThrow();
        Assertions.assertTrue(abcAssembly.getImportedModuleNames().contains("abc.ABC"));
        Assertions.assertTrue(abcAssembly.getImportedModuleNames().contains("abc.abstractmethod"));

        CodeAssembly dataclassAssembly = allAssemblies(codeModel).filter(a -> "APyDataClass".equals(a.getName())).findFirst().orElseThrow();
        Assertions.assertTrue(dataclassAssembly.getImportedModuleNames().contains("dataclasses.dataclass"));
    }

    private static Stream<CodeAssembly> allAssemblies(CodeModel codeModel) {
        return codeModel.getAllPackages().stream().flatMap(p -> p.getContent().stream()).filter(CodeAssembly.class::isInstance).map(CodeAssembly.class::cast);
    }

}
