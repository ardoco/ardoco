package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.mappers;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppExtractor;

public class CppElementManagerTest {
    private final String sourcePath = "src/test/resources/cpp/interface/edu/";

    @Test
    void testCppElementManagerMainCPP() throws IOException {
        String filePath = sourcePath + "src/main.cpp";
        CppElementManager manager = buildCppElementManager(filePath);

        // Assertions
        Assertions.assertEquals("main()", manager.getFunctions().get(0).getName());
        Assertions.assertEquals(
                "Simple C++ Project Author: Your Name Description: A basic C++ project with a simple structure.\nmain.cpp",
                manager.getFunctions().get(0).getComment());
    }

    @Test
    void testCppElementManagerEntitiesCPP() throws IOException {
        String filePath = sourcePath + "src/Entities.cpp";
        CppElementManager manager = buildCppElementManager(filePath);

        // Assertions
        Assertions.assertEquals("Entities", manager.getNamespaces().get(0).getName());
        Assertions.assertEquals("Entities.cpp", manager.getNamespaces().get(0).getComment());
    }

    @Test
    void testCppElementManagerEntitiesH() throws IOException {
        String filePath = sourcePath + "include/Entities.h";
        CppElementManager manager = buildCppElementManager(filePath);

        // Assertions
        Assertions.assertEquals("Entities", manager.getNamespaces().get(0).getName());
        Assertions.assertEquals("Entities.h", manager.getNamespaces().get(0).getComment());
    }

    private CppElementManager buildCppElementManager(String path) throws IOException {
        CodeItemRepository repository = new CodeItemRepository();
        CppExtractor extractor = new CppExtractor(repository, path);
        extractor.extractElements();
        CppElementManager manager = extractor.getElementManager();
        manager.addComments(extractor.getComments());
        return manager;
    }
}
