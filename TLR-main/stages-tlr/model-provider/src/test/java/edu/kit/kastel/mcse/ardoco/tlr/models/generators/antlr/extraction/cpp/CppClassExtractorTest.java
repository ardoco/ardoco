package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.cpp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppElementExtractor;
import generated.antlr.cpp.CPP14Lexer;

public class CppClassExtractorTest {
    String sourcePath = "src/test/resources/cpp/interface/edu/";

    @Test
    public void testCppClassExtractorMainCPPTest() throws IOException {
        String filePath = sourcePath + "src/main.cpp";
        List<ClassElement> classes = extractClassElementsFromFile(filePath);

        // Test the first class
        Assertions.assertEquals(0, classes.size());
    }

    @Test
    public void testCppClassExtractorEntitiesCPPTest() throws IOException {
        String filePath = sourcePath + "src/Entities.cpp";
        List<ClassElement> classes = extractClassElementsFromFile(filePath);

        // Test the first class
        Assertions.assertEquals(0, classes.size());
    }

    @Test
    public void testCppClassExtractorEntitiesHTest() throws IOException {
        String filePath = sourcePath + "include/Entities.h";
        List<ClassElement> classes = extractClassElementsFromFile(filePath);

        // Test the first class
        Assertions.assertEquals(4, classes.size());
        Assertions.assertEquals("Car", classes.get(0).getName());
        Assertions.assertEquals("Entities", classes.get(0).getParent().getName());
        Assertions.assertEquals(Type.NAMESPACE, classes.get(0).getParent().getType());
        Assertions.assertEquals("Person", classes.get(1).getName());
        Assertions.assertEquals("Entities", classes.get(1).getParent().getName());
        Assertions.assertEquals(Type.NAMESPACE, classes.get(1).getParent().getType());
        Assertions.assertEquals("Garage", classes.get(2).getName());
        Assertions.assertEquals("Entities", classes.get(2).getParent().getName());
        Assertions.assertEquals(Type.NAMESPACE, classes.get(2).getParent().getType());
        Assertions.assertEquals("Child", classes.get(3).getName());
        Assertions.assertEquals("Entities", classes.get(3).getParent().getName());
        Assertions.assertEquals(Type.NAMESPACE, classes.get(3).getParent().getType());
        Assertions.assertEquals(1, classes.get(3).getInherits().size());
        Assertions.assertEquals("Person", classes.get(3).getInherits().get(0));
    }

    private List<ClassElement> extractClassElementsFromFile(String filePath) throws IOException {
        CppElementExtractor extractor = new CppElementExtractor();
        Path path = Path.of(filePath);
        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromPath(path));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        extractor.extract(tokenStream);
        return extractor.getElements().getClasses();
    }
}
