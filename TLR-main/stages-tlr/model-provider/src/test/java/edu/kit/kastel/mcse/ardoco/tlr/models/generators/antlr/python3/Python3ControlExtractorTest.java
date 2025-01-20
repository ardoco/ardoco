package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.python3;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3.Python3ControlExtractor;
import generated.antlr.Python3Lexer;
import generated.antlr.Python3Parser;
import generated.antlr.Python3Parser.File_inputContext;

public class Python3ControlExtractorTest {
    String sourcePath = "src/test/resources/python/interface/edu/";

    @Test
    public void testPyAClassControlExtractor() throws IOException {
        String filePath = sourcePath + "APyClass.py";
        List<ControlElement> controls = extractControlElementsFromFile(filePath);
        Assertions.assertEquals(6, controls.size());

        // Test the first control
        Assertions.assertEquals("__init__", controls.get(0).getName());
        Assertions.assertEquals("AClass", controls.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, controls.get(0).getParent().getType());

        // Test the second control
        Assertions.assertEquals("display_name", controls.get(1).getName());
        Assertions.assertEquals("AClass", controls.get(1).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, controls.get(1).getParent().getType());

        // Test the third control
        Assertions.assertEquals("__init__", controls.get(2).getName());
        Assertions.assertEquals("InnerClass1", controls.get(2).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, controls.get(2).getParent().getType());

        // Test the fourth control
        Assertions.assertEquals("display_value", controls.get(3).getName());
        Assertions.assertEquals("InnerClass1", controls.get(3).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, controls.get(3).getParent().getType());

        // Test the fifth control
        Assertions.assertEquals("__init__", controls.get(4).getName());
        Assertions.assertEquals("InnerClass2", controls.get(4).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, controls.get(4).getParent().getType());

        // Test the sixth control
        Assertions.assertEquals("display_value", controls.get(5).getName());
        Assertions.assertEquals("InnerClass2", controls.get(5).getParent().getName());
        Assertions.assertEquals(BasicType.CLASS, controls.get(5).getParent().getType());
        
    }
    
    private List<ControlElement> extractControlElementsFromFile(String filePath) throws IOException {
        // Create a CompilationUnitContext from the source file
        Python3Lexer lexer = new Python3Lexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        File_inputContext ctx = parser.file_input();

        // Create a VariableExtractor and visit the File_inputContext
        Python3ControlExtractor extractor = new Python3ControlExtractor(filePath);
        return extractor.visitFile_input(ctx);
    }
}
