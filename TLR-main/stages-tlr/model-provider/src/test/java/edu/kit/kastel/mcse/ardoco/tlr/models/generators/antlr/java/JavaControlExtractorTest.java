package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.java;



import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java.JavaControlExtractor;
import generated.antlr.JavaLexer;
import generated.antlr.JavaParser;
import generated.antlr.JavaParser.CompilationUnitContext;

class JavaControlExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(JavaCompilationUnitExtractorTest.class);
    private final String sourcePath = "src/test/resources/interface/edu/";
    
    @Test
    void controlExtractorAClassTest() throws IOException {
        String filePath = sourcePath + "AClass.java";
        List<ControlElement> controls = extractControlElementsFromFile(filePath);
        Assertions.assertEquals(1, controls.size());
        Assertions.assertEquals("aMethod", controls.get(0).getName());
        Assertions.assertEquals("AClass", controls.get(0).getParent().getName());
    }


    private List<ControlElement> extractControlElementsFromFile(String filePath) throws IOException {
        JavaLexer lexer = new JavaLexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        CompilationUnitContext ctx = parser.compilationUnit();
        
        JavaControlExtractor extractor = new JavaControlExtractor();
        return extractor.visitCompilationUnit(ctx);

    }

}
