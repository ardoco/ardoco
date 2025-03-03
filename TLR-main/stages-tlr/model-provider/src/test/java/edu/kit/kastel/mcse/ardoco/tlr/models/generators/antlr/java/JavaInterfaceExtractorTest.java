package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.java;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java.JavaInterfaceExtractor;
import generated.antlr.java.JavaLexer;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParser.CompilationUnitContext;

public class JavaInterfaceExtractorTest {
    private final String sourcePath = "src/test/resources/interface/edu/";

    @Test
    void interfaceExtractorAnInterfaceTest() throws IOException {
        String filePath = sourcePath + "AnInterface.java";
        List<InterfaceElement> interfaces = extractInterfacesFromFile(filePath);
        Assertions.assertEquals(1, interfaces.size());
        Assertions.assertEquals("AnInterface", interfaces.get(0).getName());
        Assertions.assertEquals("AnInterface", interfaces.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.COMPILATIONUNIT, interfaces.get(0).getParent().getType());
    }

    @Test
    void interfaceExtractorExtendedInterface() throws IOException {
        String filePath = sourcePath + "ExtendedInterface.java";
        List<InterfaceElement> interfaces = extractInterfacesFromFile(filePath);
        Assertions.assertEquals(1, interfaces.size());
        Assertions.assertEquals("ExtendedInterface", interfaces.get(0).getName());
        Assertions.assertEquals("ExtendedInterface", interfaces.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.COMPILATIONUNIT, interfaces.get(0).getParent().getType());
    }

    @Test
    void interfaceExtractorOtherInterfaceZwei() throws IOException {
        String filePath = sourcePath + "zwei/OtherInterface.java";
        List<InterfaceElement> interfaces = extractInterfacesFromFile(filePath);
        Assertions.assertEquals(1, interfaces.size());
        Assertions.assertEquals("OtherInterface", interfaces.get(0).getName());
        Assertions.assertEquals("OtherInterface", interfaces.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.COMPILATIONUNIT, interfaces.get(0).getParent().getType());
    }

    @Test
    void interfaceExtractorOtherInterfaceDrei() throws IOException {
        String filePath = sourcePath + "drei/OtherInterface.java";
        List<InterfaceElement> interfaces = extractInterfacesFromFile(filePath);
        Assertions.assertEquals(1, interfaces.size());
        Assertions.assertEquals("OtherInterface", interfaces.get(0).getName());
        Assertions.assertEquals("OtherInterface", interfaces.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.COMPILATIONUNIT, interfaces.get(0).getParent().getType());
    }

    @Test
    void interfaceExtractorAClass() throws IOException {
        String filePath = sourcePath + "AClass.java";
        List<InterfaceElement> interfaces = extractInterfacesFromFile(filePath);
        Assertions.assertTrue(interfaces.isEmpty());
    }

    @Test
    void interfaceExtractorSuperclass() throws IOException {
        String filePath = sourcePath + "Superclass.java";
        List<InterfaceElement> interfaces = extractInterfacesFromFile(filePath);
        Assertions.assertTrue(interfaces.isEmpty());
    }

    @Test
    void interfaceExtractorAnEnum() throws IOException {
        String filePath = sourcePath + "AnEnum.java";
        List<InterfaceElement> interfaces = extractInterfacesFromFile(filePath);
        Assertions.assertTrue(interfaces.isEmpty());
    }

    private List<InterfaceElement> extractInterfacesFromFile(String filePath) throws IOException {
        JavaLexer lexer = new JavaLexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        CompilationUnitContext ctx = parser.compilationUnit();

        JavaInterfaceExtractor extractor = new JavaInterfaceExtractor();
        return extractor.visit(ctx);

    }

}
