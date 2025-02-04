package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.python3;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ModuleElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3.Python3ModuleExtractor;
import generated.antlr.Python3Lexer;
import generated.antlr.Python3Parser;
import generated.antlr.Python3Parser.File_inputContext;


public class Python3ModuleExtractorTest {
    String sourcePath = "src/test/resources/python/interface/edu/";
    
    @Test
    void testPyClassModuleExtraction() throws IOException {
        String filePath = sourcePath + "APyClass.py";
        List<Python3ModuleElement> modules = extractModuleElement(filePath);
        // Assertions
        Assertions.assertEquals(1, modules.size());
        Assertions.assertEquals("APyClass", modules.get(0).getName());
        Assertions.assertEquals("src/test/resources/python/interface/edu/", modules.get(0).getPathString());
        Assertions.assertEquals("edu", modules.get(0).getPackageName());
    }

    @Test
    void testOtherPyAbstractBaseClassModuleExtraction() throws IOException {
        String filePath = sourcePath + "drei/OtherPyAbstractBaseClass.py";
        List<Python3ModuleElement> modules = extractModuleElement(filePath);
        // Assertions
        Assertions.assertEquals(1, modules.size());
        Assertions.assertEquals("OtherPyAbstractBaseClass", modules.get(0).getName());
        Assertions.assertEquals("src/test/resources/python/interface/edu/drei/", modules.get(0).getPathString());
        Assertions.assertEquals("edu/drei", modules.get(0).getPackageName());
    }

    private List<Python3ModuleElement> extractModuleElement(String filePath) throws IOException {
        // Create a CompilationUnitContext from the source file
        Python3Lexer lexer = new Python3Lexer(CharStreams.fromFileName(filePath));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        File_inputContext ctx = parser.file_input();

        // Create a VariableExtractor and visit the File_inputContext
        Python3ModuleExtractor extractor = new Python3ModuleExtractor(sourcePath);
        return extractor.visitFile_input(ctx);
    }
}
