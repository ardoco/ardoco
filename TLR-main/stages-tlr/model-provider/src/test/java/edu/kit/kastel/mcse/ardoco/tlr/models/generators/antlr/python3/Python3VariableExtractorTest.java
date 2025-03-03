package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.python3;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3.Python3VariableExtractor;
import generated.antlr.python3.Python3Lexer;
import generated.antlr.python3.Python3Parser;
import generated.antlr.python3.Python3Parser.File_inputContext;

public class Python3VariableExtractorTest {
    String sourcePath = "src/test/resources/python/interface/edu/";

    @Test
    void testPyAClassVariableExtractor() throws IOException {
        String filePath = sourcePath + "APyClass.py";
        List<Python3VariableElement> variables = extractVariablesFromFile(filePath);
        Assertions.assertEquals(10, variables.size());

        // Test the first variable
        Assertions.assertEquals("class_variable", variables.get(0).getName());
        Assertions.assertEquals("str", variables.get(0).getType());
        Assertions.assertEquals("AClass", variables.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, variables.get(0).getParent().getType());

        // Test the second variable
        Assertions.assertEquals("self.name", variables.get(1).getName());
        Assertions.assertEquals("any", variables.get(1).getType());
        Assertions.assertEquals("__init__", variables.get(1).getParent().getName());
        Assertions.assertEquals(BasicType.CONTROL, variables.get(1).getParent().getType());

        // Test the third variable
        Assertions.assertEquals("self.instance_variable", variables.get(2).getName());
        Assertions.assertEquals("str", variables.get(2).getType());
        Assertions.assertEquals("__init__", variables.get(2).getParent().getName());
        Assertions.assertEquals(BasicType.CONTROL, variables.get(2).getParent().getType());

        // Test the fourth variable
        Assertions.assertEquals("class_variable", variables.get(3).getName());
        Assertions.assertEquals("str", variables.get(3).getType());
        Assertions.assertEquals("InnerClass1", variables.get(3).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, variables.get(3).getParent().getType());

        // Test the fifth variable
        Assertions.assertEquals("self.value", variables.get(4).getName());
        Assertions.assertEquals("any", variables.get(4).getType());
        Assertions.assertEquals("__init__", variables.get(4).getParent().getName());
        Assertions.assertEquals(BasicType.CONTROL, variables.get(4).getParent().getType());

        // Test the sixth variable
        Assertions.assertEquals("self.instance_variable", variables.get(5).getName());
        Assertions.assertEquals("str", variables.get(5).getType());
        Assertions.assertEquals("__init__", variables.get(5).getParent().getName());
        Assertions.assertEquals(BasicType.CONTROL, variables.get(5).getParent().getType());

        // Test the seventh variable
        Assertions.assertEquals("class_variable", variables.get(6).getName());
        Assertions.assertEquals("str", variables.get(6).getType());
        Assertions.assertEquals("InnerClass2", variables.get(6).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, variables.get(6).getParent().getType());

        // Test the eighth variable
        Assertions.assertEquals("self.value", variables.get(7).getName());
        Assertions.assertEquals("any", variables.get(7).getType());
        Assertions.assertEquals("__init__", variables.get(7).getParent().getName());
        Assertions.assertEquals(BasicType.CONTROL, variables.get(7).getParent().getType());

        // Test the ninth variable
        Assertions.assertEquals("self.instance_variable", variables.get(8).getName());
        Assertions.assertEquals("str", variables.get(8).getType());
        Assertions.assertEquals("__init__", variables.get(8).getParent().getName());
        Assertions.assertEquals(BasicType.CONTROL, variables.get(8).getParent().getType());
        
        // Test the tenth variable
        Assertions.assertEquals("class_variable", variables.get(9).getName());
        Assertions.assertEquals("str", variables.get(9).getType());
        Assertions.assertEquals("BClass", variables.get(9).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, variables.get(9).getParent().getType());
    }

    private List<Python3VariableElement> extractVariablesFromFile(String filePath) throws IOException {
        // Create a CompilationUnitContext from the source file
        Python3Lexer lexer = new Python3Lexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        File_inputContext ctx = parser.file_input();

        // Create a VariableExtractor and visit the File_inputContext
        Python3VariableExtractor extractor = new Python3VariableExtractor();
        return extractor.visitFile_input(ctx);
    }

    
    
}
