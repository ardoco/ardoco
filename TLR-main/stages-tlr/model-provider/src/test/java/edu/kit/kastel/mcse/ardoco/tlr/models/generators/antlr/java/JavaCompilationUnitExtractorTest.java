package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.java;

import org.junit.jupiter.api.Assertions;

import java.io.IOException;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CompilationUnitElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java.JavaCompilationUnitExtractor;
import generated.antlr.JavaLexer;
import generated.antlr.JavaParser;
import generated.antlr.JavaParser.CompilationUnitContext;

class JavaCompilationUnitExtractorTest {
    private final String sourcePath = "src/test/resources/interface/edu/";

    @Test
    void compilationUnitAClassTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "AClass.java");
        Assertions.assertEquals("AClass", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/AClass.java", element.getPath());
        Assertions.assertEquals("edu", element.getPackage().getName());
    }

    @Test
    void compilationUnitAnEnumTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "AnEnum.java");
        Assertions.assertEquals("AnEnum", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/AnEnum.java", element.getPath());
        Assertions.assertEquals("edu", element.getPackage().getName());
    }

    @Test
    void compilationUnitAnInterfaceTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "AnInterface.java");
        Assertions.assertEquals("AnInterface", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/AnInterface.java", element.getPath());
        Assertions.assertEquals("edu", element.getPackage().getName());
    }

    @Test
    void compilationUnitExtendedInterfaceTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "ExtendedInterface.java");
        Assertions.assertEquals("ExtendedInterface", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/ExtendedInterface.java", element.getPath());
        Assertions.assertEquals("edu", element.getPackage().getName());
    }

    @Test
    void compilationUnitSuperclassTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "Superclass.java");
        Assertions.assertEquals("Superclass", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/Superclass.java", element.getPath());
        Assertions.assertEquals("edu", element.getPackage().getName());
    }

    @Test
    void compilationUnitOtherInterfaceZweiTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "zwei/OtherInterface.java");
        Assertions.assertEquals("OtherInterface", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/zwei/OtherInterface.java", element.getPath());
        Assertions.assertEquals("edu.zwei", element.getPackage().getName());
    }

    @Test
    void compilationUnitOtherInterfaceDreiTest() throws IOException {
        CompilationUnitElement element = compilationUnitExtractorTest(sourcePath + "drei/OtherInterface.java");
        Assertions.assertEquals("OtherInterface", element.getName());
        Assertions.assertEquals("src/test/resources/interface/edu/drei/OtherInterface.java", element.getPath());
        Assertions.assertEquals("edu.drei", element.getPackage().getName());
    }



    private CompilationUnitElement compilationUnitExtractorTest(String filePath) throws IOException {
        // Create a CompilationUnitContext from the source file
        JavaLexer lexer = new JavaLexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        CompilationUnitContext ctx = parser.compilationUnit();

        // Create a JavaCompilationUnitExtractor and visit the CompilationUnitContext
        JavaCompilationUnitExtractor extractor = new JavaCompilationUnitExtractor();
        return extractor.visitCompilationUnit(ctx);
    }


    
}
