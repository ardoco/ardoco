package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.mappers;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.JavaElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java.JavaExtractor;

public class JavaElementManagerTest {
    private final String sourcePath = "src/test/resources/interface/edu/";

    @Test
    void testJavaElementManagerAClass() throws IOException {
        String filePath = sourcePath + "AClass.java";
        JavaElementManager manager = testJavaCommentMapper(filePath);
        // Assertions
        Assertions.assertEquals("s", manager.getVariables().get(0).getName());
        Assertions.assertEquals("This is a Test Line Comment", manager.getVariables().get(0).getComment());

        Assertions.assertEquals("AClass", manager.getClasses().get(2).getName());
        Assertions.assertEquals("This is a Test Java Doc Comment", manager.getClasses().get(2).getComment());
        Assertions.assertEquals("aMethod", manager.getFunctions().get(0).getName());
        Assertions.assertEquals("This is a Test Block Comment", manager.getFunctions().get(0).getComment());
    }

    @Test
    void testJavaElementManagerSuperClass() throws IOException {
        String filePath = sourcePath + "Superclass.java";
        JavaElementManager manager = testJavaCommentMapper(filePath);

        // Assertions
        Assertions.assertEquals("Superclass", manager.getClasses().get(0).getName());
        Assertions.assertEquals("This is a Test Java Doc Comment over multiple lines",
                manager.getClasses().get(0).getComment());
    }

    private JavaElementManager testJavaCommentMapper(String path) throws IOException {
        CodeItemRepository repository = new CodeItemRepository();
        JavaExtractor extractor = new JavaExtractor(repository, path);
        extractor.extractElements();
        JavaElementManager manager = extractor.getElementManager();
        manager.addComments(extractor.getComments());
        return manager;
    }

}
