package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.java;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java.JavaElementExtractor;
import generated.antlr.java.JavaLexer;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParser.CompilationUnitContext;

class JavaControlExtractorTest {
    private final String sourcePath = "src/test/resources/interface/edu/";

    @Test
    void controlExtractorAClassTest() throws IOException {
        String filePath = sourcePath + "AClass.java";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertEquals(1, controls.size());
        Assertions.assertEquals("aMethod", controls.get(0).getName());
        Assertions.assertEquals("AClass", controls.get(0).getParent().getName());
    }

    @Test
    void controlExtractorAnEnum() throws IOException {
        String filePath = sourcePath + "AnEnum.java";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertTrue(controls.isEmpty());
    }

    @Test
    void controlExtractorAnInterface() throws IOException {
        String filePath = sourcePath + "AnInterface.java";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertTrue(controls.isEmpty());
    }

    @Test
    void controlExtractorSuperclass() throws IOException {
        String filePath = sourcePath + "Superclass.java";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertTrue(controls.isEmpty());
    }

    @Test
    void controlExtractorExtendedInterface() throws IOException {
        String filePath = sourcePath + "ExtendedInterface.java";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertTrue(controls.isEmpty());
    }

    @Test
    void controlExtractorOtherInterfaceZwei() throws IOException {
        String filePath = sourcePath + "zwei/OtherInterface.java";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertTrue(controls.isEmpty());
    }

    @Test
    void controlExtractorOtherInterfaceDrei() throws IOException {
        String filePath = sourcePath + "drei/OtherInterface.java";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertTrue(controls.isEmpty());
    }

    private List<Element> extractBasicElementsFromFile(String filePath) throws IOException {
        JavaLexer lexer = new JavaLexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        CompilationUnitContext ctx = parser.compilationUnit();

        JavaElementExtractor extractor = new JavaElementExtractor();
        extractor.extract(ctx);
        return extractor.getElementManager().getFunctions();
    }

}
