package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.mappers;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3.Python3Extractor;

public class Python3ElementManagerTest {
    private final String sourcePath = "src/test/resources/python/interface/edu/";

    @Test
    void testPython3ElementManagerAClass() throws IOException {
        String filePath = sourcePath + "APyClass.py";
        Python3ElementManager manager = testPython3ElementManager(filePath);
        // Assertions
        Assertions.assertEquals("AClass", manager.getClasses().get(2).getName());
        Assertions.assertEquals("This is a Comment for AClass", manager.getClasses().get(2).getComment());

        Assertions.assertEquals("class_variable", manager.getVariables().get(0).getName());
        Assertions.assertEquals("This is an inline comment for class_variable of AClass",
                manager.getVariables().get(0).getComment());

        Assertions.assertEquals("InnerClass1", manager.getClasses().get(0).getName());
        Assertions.assertEquals("This is a multiple line comment for InnerClass1",
                manager.getClasses().get(0).getComment());

        Assertions.assertEquals("InnerClass2", manager.getClasses().get(1).getName());
        Assertions.assertEquals("This is a multiple line comment for InnerClass2",
                manager.getClasses().get(1).getComment());
    }

    private Python3ElementManager testPython3ElementManager(String path) throws IOException {
        CodeItemRepository repository = new CodeItemRepository();
        Python3Extractor extractor = new Python3Extractor(repository, path);
        extractor.extractElements();
        Python3ElementManager manager = extractor.getElementManager();
        manager.addComments(extractor.getComments());

        return manager;
    }

}
