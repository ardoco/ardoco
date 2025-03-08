package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.python3;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3.Python3ElementExtractor;
import generated.antlr.python3.Python3Lexer;
import generated.antlr.python3.Python3Parser;
import generated.antlr.python3.Python3Parser.File_inputContext;

public class Python3ControlExtractorTest {
    String sourcePath = "src/test/resources/python/interface/edu/";

    @Test
    void testPyAClassControlExtractor() throws IOException {
        String filePath = sourcePath + "APyClass.py";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertEquals(6, controls.size());

        // Test the first control
        Assertions.assertEquals("__init__", controls.get(0).getName());
        Assertions.assertEquals("AClass", controls.get(0).getParent().getName());
        Assertions.assertEquals(Type.CLASS, controls.get(0).getParent().getType());

        // Test the second control
        Assertions.assertEquals("display_name", controls.get(1).getName());
        Assertions.assertEquals("AClass", controls.get(1).getParent().getName());
        Assertions.assertEquals(Type.CLASS, controls.get(1).getParent().getType());

        // Test the third control
        Assertions.assertEquals("__init__", controls.get(2).getName());
        Assertions.assertEquals("InnerClass1", controls.get(2).getParent().getName());
        Assertions.assertEquals(Type.CLASS, controls.get(2).getParent().getType());

        // Test the fourth control
        Assertions.assertEquals("display_value", controls.get(3).getName());
        Assertions.assertEquals("InnerClass1", controls.get(3).getParent().getName());
        Assertions.assertEquals(Type.CLASS, controls.get(3).getParent().getType());

        // Test the fifth control
        Assertions.assertEquals("__init__", controls.get(4).getName());
        Assertions.assertEquals("InnerClass2", controls.get(4).getParent().getName());
        Assertions.assertEquals(Type.CLASS, controls.get(4).getParent().getType());

        // Test the sixth control
        Assertions.assertEquals("display_value", controls.get(5).getName());
        Assertions.assertEquals("InnerClass2", controls.get(5).getParent().getName());
        Assertions.assertEquals(Type.CLASS, controls.get(5).getParent().getType());
    }

    @Test
    void testPyModuleControlExtractor() throws IOException {
        String filePath = sourcePath + "APyModule.py";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertEquals(3, controls.size());

        // Test the first control
        Assertions.assertEquals("greet", controls.get(0).getName());
        Assertions.assertEquals("APyModule", controls.get(0).getParent().getName());
        Assertions.assertEquals(Type.MODULE, controls.get(0).getParent().getType());

        // Test the second control
        Assertions.assertEquals("add", controls.get(1).getName());
        Assertions.assertEquals("APyModule", controls.get(1).getParent().getName());
        Assertions.assertEquals(Type.MODULE, controls.get(1).getParent().getType());

        // Test the third control
        Assertions.assertEquals("subtract", controls.get(2).getName());
        Assertions.assertEquals("APyModule", controls.get(2).getParent().getName());
        Assertions.assertEquals(Type.MODULE, controls.get(2).getParent().getType());
    }

    @Test
    void testPyMetaclassExtractor() throws IOException {
        String filePath = sourcePath + "APyMetaclass.py";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertEquals(2, controls.size());

        // Test the first control
        Assertions.assertEquals("__new__", controls.get(0).getName());
        Assertions.assertEquals("APyMetaclass", controls.get(0).getParent().getName());
        Assertions.assertEquals(Type.CLASS, controls.get(0).getParent().getType());

        // Test the second control
        Assertions.assertEquals("__init__", controls.get(1).getName());
        Assertions.assertEquals("APyMetaclass", controls.get(1).getParent().getName());
        Assertions.assertEquals(Type.CLASS, controls.get(1).getParent().getType());
    }

    @Test
    void testPyEnumControlExtractor() throws IOException {
        String filePath = sourcePath + "APyEnum.py";
        List<Element> controls = extractBasicElementsFromFile(filePath);
        Assertions.assertEquals(0, controls.size());
    }

    private List<Element> extractBasicElementsFromFile(String filePath) throws IOException {
        // Create a CompilationUnitContext from the source file
        Python3Lexer lexer = new Python3Lexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        File_inputContext ctx = parser.file_input();

        // Create a VariableExtractor and visit the File_inputContext
        Python3ElementExtractor extractor = new Python3ElementExtractor();
        extractor.extract(ctx);
        return extractor.getElementManager().getFunctions();
    }
}
