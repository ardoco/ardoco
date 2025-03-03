package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.cpp;



import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.cpp.CppExtractor;

public class CppExtractorTest {
    @Test
    void executeCppExtractorForMinimalDirectoryTest() throws IOException {
        String sourcePath = "src/test/resources/cpp/interface/edu/";
        CppExtractor cppExtractor = buildCppExtractor(sourcePath);
        cppExtractor.execute();

        // Assertions
        Assertions.assertEquals(9 , cppExtractor.getVariables().size());
        Assertions.assertEquals(10 , cppExtractor.getControls().size());
        Assertions.assertEquals(2 , cppExtractor.getNamespaces().size());
        Assertions.assertEquals(4, cppExtractor.getClasses().size());
        Assertions.assertEquals(7, cppExtractor.getComments().size());
    }

    private CppExtractor buildCppExtractor(String sourcePath) {
        CodeItemRepository repository = new CodeItemRepository();
        return new CppExtractor(repository, sourcePath);
    }
    
}
