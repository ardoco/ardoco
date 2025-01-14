package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.java;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CompilationUnitElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java.JavaCompilationUnitExtractor;
import generated.antlr.JavaLexer;
import generated.antlr.JavaParser;
import generated.antlr.JavaParser.CompilationUnitContext;

class JavaCompilationUnitExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(JavaCompilationUnitExtractorTest.class);
    private final String sourcePath = "src/test/resources/interface/edu/";

    @Test
    void compilationUnitAClassTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "AClass.java");
        Assertions.assertEquals("AClass", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/", element.getPathString());
        Assertions.assertEquals("edu", element.getPackageName());
    }

    @Test
    void compilationUnitAnEnumTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "AnEnum.java");
        Assertions.assertEquals("AnEnum", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/", element.getPathString());
        Assertions.assertEquals("edu", element.getPackageName());
    }

    @Test
    void compilationUnitAnInterfaceTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "AnInterface.java");
        Assertions.assertEquals("AnInterface", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/", element.getPathString());
        Assertions.assertEquals("edu", element.getPackageName());
    }

    @Test
    void compilationUnitExtendedInterfaceTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "ExtendedInterface.java");
        Assertions.assertEquals("ExtendedInterface", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/", element.getPathString());
        Assertions.assertEquals("edu", element.getPackageName());
    }

    @Test
    void compilationUnitSuperclassTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "Superclass.java");
        Assertions.assertEquals("Superclass", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/", element.getPathString());
        Assertions.assertEquals("edu", element.getPackageName());
    }

    @Test
    void compilationUnitOtherInterfaceZweiTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "zwei/OtherInterface.java");
        Assertions.assertEquals("OtherInterface", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/zwei/", element.getPathString());
        Assertions.assertEquals("edu.zwei", element.getPackageName());
    }

    @Test
    void compilationUnitOtherInterfaceDreiTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "drei/OtherInterface.java");
        Assertions.assertEquals("OtherInterface", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/drei/", element.getPathString());
        Assertions.assertEquals("edu.drei", element.getPackageName());
    }



    private CompilationUnitElement compilationUnitExtractorTest(String filePath) throws IOException {
        // Create a CompilationUnitContext from the source file
        JavaLexer lexer = new JavaLexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        CompilationUnitContext ctx = parser.compilationUnit();

        // Create a JavaCompilationUnitExtractor and visit the CompilationUnitContext
        JavaCompilationUnitExtractor extractor = new JavaCompilationUnitExtractor(filePath);
        return extractor.visitCompilationUnit(ctx);
    }


    
}
