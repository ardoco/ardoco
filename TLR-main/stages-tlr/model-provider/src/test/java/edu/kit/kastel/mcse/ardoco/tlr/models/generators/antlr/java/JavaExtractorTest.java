package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.java;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java.JavaExtractor;

public class JavaExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(JavaCompilationUnitExtractorTest.class);
    
    @Test
    void executeJavaExtractorForMinimalDirectoryTest() throws IOException {
        String sourcePath = "src/test/resources/interface/edu/";
        JavaExtractor javaExtractor = buildJavaExtractor(sourcePath);
        javaExtractor.execute();

        // Assertions
        Assertions.assertEquals(3 , javaExtractor.getVariables().size());
        Assertions.assertEquals(1 , javaExtractor.getControls().size());
        Assertions.assertEquals(6 , javaExtractor.getClasses().size());
        Assertions.assertEquals(4 , javaExtractor.getInterfaces().size());
        Assertions.assertEquals(7 , javaExtractor.getCompilationUnits().size());
    }


    private JavaExtractor buildJavaExtractor(String sourcePath) {
        CodeItemRepository repository = new CodeItemRepository();
        return new JavaExtractor(repository, sourcePath);
    }

}
