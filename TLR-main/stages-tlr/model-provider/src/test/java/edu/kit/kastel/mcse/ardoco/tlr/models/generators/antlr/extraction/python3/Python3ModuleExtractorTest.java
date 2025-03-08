package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.python3;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3.Python3ElementExtractor;
import generated.antlr.python3.Python3Lexer;
import generated.antlr.python3.Python3Parser;
import generated.antlr.python3.Python3Parser.File_inputContext;

public class Python3ModuleExtractorTest {
    String sourcePath = "src/test/resources/python/interface/edu/";

    @Test
    void testPyClassModuleExtraction() throws IOException {
        String filePath = sourcePath + "APyClass.py";
        List<Element> modules = extractModuleElement(filePath);
        // Assertions
        Assertions.assertEquals(1, modules.size());
        Assertions.assertEquals("APyClass", modules.get(0).getName());
        Assertions.assertEquals("src/test/resources/python/interface/edu/APyClass.py", modules.get(0).getPath());
        Assertions.assertEquals(Type.PACKAGE, modules.get(0).getParent().getType());
    }

    @Test
    void testOtherPyAbstractBaseClassModuleExtraction() throws IOException {
        String filePath = sourcePath + "drei/OtherPyAbstractBaseClass.py";
        List<Element> modules = extractModuleElement(filePath);
        // Assertions
        Assertions.assertEquals(1, modules.size());
        Assertions.assertEquals("OtherPyAbstractBaseClass", modules.get(0).getName());
        Assertions.assertEquals("src/test/resources/python/interface/edu/drei/OtherPyAbstractBaseClass.py",
                modules.get(0).getPath());
        Assertions.assertEquals(Type.PACKAGE, modules.get(0).getParent().getType());
    }

    private List<Element> extractModuleElement(String filePath) throws IOException {
        // Create a CompilationUnitContext from the source file
        Python3Lexer lexer = new Python3Lexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        File_inputContext ctx = parser.file_input();

        // Create a VariableExtractor and visit the File_inputContext
        Python3ElementExtractor extractor = new Python3ElementExtractor();
        extractor.extract(ctx);
        return extractor.getElementManager().getModules();
    }
}
