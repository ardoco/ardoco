package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.cpp;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppExtractor;

public class CppExtractorTest {
    @Test
    void executeCppExtractorForMinimalDirectoryTest() throws IOException {
        String sourcePath = "src/test/resources/cpp/interface/edu/";
        CppExtractor cppExtractor = buildCppExtractor(sourcePath);
        cppExtractor.extractModel();
        CppElementManager manager = cppExtractor.getElementManager();

        // Assertions
        Assertions.assertEquals(9, manager.getVariables().size());
        Assertions.assertEquals(10, manager.getFunctions().size());
        Assertions.assertEquals(2, manager.getNamespaces().size());
        Assertions.assertEquals(4, manager.getClasses().size());
        Assertions.assertEquals(7, cppExtractor.getComments().size());
    }

    private CppExtractor buildCppExtractor(String sourcePath) {
        CodeItemRepository repository = new CodeItemRepository();
        return new CppExtractor(repository, sourcePath);
    }

}
