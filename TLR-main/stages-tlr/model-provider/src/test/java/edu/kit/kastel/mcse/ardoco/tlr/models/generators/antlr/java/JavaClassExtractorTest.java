package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.java;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java.JavaClassExtractor;
import generated.antlr.java.JavaLexer;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParser.CompilationUnitContext;

class JavaClassExtractorTest {
    private final String sourcePath = "src/test/resources/interface/edu/";
    
    @Test
    void classExtractorAClassTest() throws IOException{
        String filePath = sourcePath + "AClass.java";
        List<JavaClassElement> classes = extractClassesFromFile(filePath);
        Assertions.assertEquals(4, classes.size());
        //Assertions Class AClass
        Assertions.assertEquals("AClass", classes.get(0).getName());
        Assertions.assertEquals("AClass", classes.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.COMPILATIONUNIT, classes.get(0).getParent().getType());
        Assertions.assertEquals("Superclass", classes.get(0).getExtendsClass());
        Assertions.assertEquals("AnInterface", classes.get(0).getImplementedInterfaces().get(0));
        Assertions.assertEquals("OtherInterface", classes.get(0).getImplementedInterfaces().get(1));

        //Assertions Class AnInnerClass
        Assertions.assertEquals("AnInnerClass", classes.get(1).getName());
        Assertions.assertEquals("AClass", classes.get(1).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, classes.get(1).getParent().getType());
        //Assertions Class AnotherClass (InnerClass)
        Assertions.assertEquals("AnotherClass", classes.get(2).getName());
        Assertions.assertEquals("AClass", classes.get(2).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, classes.get(2).getParent().getType());
        //Assertions Class AnotherClass 
        Assertions.assertEquals("AnotherClass", classes.get(3).getName());
        Assertions.assertEquals("AClass", classes.get(3).getParent().getName());
        Assertions.assertEquals(BasicType.COMPILATIONUNIT, classes.get(3).getParent().getType());
    }

    @Test
    void classExtractorSuperclass() throws IOException{
        String filePath = sourcePath + "Superclass.java";
        List<JavaClassElement> classes = extractClassesFromFile(filePath);
        Assertions.assertEquals(1, classes.size());
        Assertions.assertEquals("Superclass", classes.get(0).getName());
        Assertions.assertEquals("Superclass", classes.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.COMPILATIONUNIT, classes.get(0).getParent().getType());
    }

    @Test
    void classExtractorAnEnum() throws IOException{
        String filePath = sourcePath + "AnEnum.java";
        List<JavaClassElement> classes = extractClassesFromFile(filePath);
        Assertions.assertEquals(1, classes.size());
        Assertions.assertEquals("AnEnum", classes.get(0).getName());
        Assertions.assertEquals("AnEnum", classes.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.COMPILATIONUNIT, classes.get(0).getParent().getType());
    }

    @Test
    void classExtractorAnInterface() throws IOException{
        String filePath = sourcePath + "AnInterface.java";
        List<JavaClassElement> classes = extractClassesFromFile(filePath);
        Assertions.assertTrue(classes.isEmpty());
    }

    @Test
    void classExtractorExtendedInterface() throws IOException{
        String filePath = sourcePath + "ExtendedInterface.java";
        List<JavaClassElement> classes = extractClassesFromFile(filePath);
        Assertions.assertTrue(classes.isEmpty());
    }



    @Test
    void classExtractorOtherInterfaceZwei() throws IOException{
        String filePath = sourcePath + "zwei/OtherInterface.java";
        List<JavaClassElement> classes = extractClassesFromFile(filePath);
        Assertions.assertTrue(classes.isEmpty());
    }

    @Test
    void classExtractorOtherInterfaceDrei() throws IOException{
        String filePath = sourcePath + "drei/OtherInterface.java";
        List<JavaClassElement> classes = extractClassesFromFile(filePath);
        Assertions.assertTrue(classes.isEmpty());
    }

    private List<JavaClassElement> extractClassesFromFile(String filePath) throws IOException {
        JavaLexer lexer = new JavaLexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        CompilationUnitContext ctx = parser.compilationUnit();

        JavaClassExtractor extractor = new JavaClassExtractor();
        return extractor.visitCompilationUnit(ctx);
    }

}
