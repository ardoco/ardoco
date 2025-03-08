package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.cpp;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppElementExtractor;
import generated.antlr.cpp.CPP14Lexer;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14Parser.TranslationUnitContext;

public class CppFileExtractorTest {
    private final String sourcePath = "src/test/resources/cpp/interface/edu/";

    @Test
    void fileExtractorMainCPPTest() throws IOException {
        String filePath = sourcePath + "src/main.cpp";
        List<Element> files = extractFileFromFile(filePath);

        Assertions.assertEquals(1, files.size());
        Assertions.assertEquals("main", files.get(0).getName());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/src/main.cpp", files.get(0).getPath());
        Assertions.assertNull(files.get(0).getParent());
    }

    @Test
    void fileExtractorEntitiesCPPTest() throws IOException {
        String filePath = sourcePath + "src/Entities.cpp";
        List<Element> files = extractFileFromFile(filePath);

        Assertions.assertEquals(1, files.size());
        Assertions.assertEquals("Entities", files.get(0).getName());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/src/Entities.cpp", files.get(0).getPath());
        Assertions.assertNull(files.get(0).getParent());
    }

    @Test
    void fileExtractorEntitiesHTest() throws IOException {
        String filePath = sourcePath + "include/Entities.h";
        List<Element> files = extractFileFromFile(filePath);

        Assertions.assertEquals(1, files.size());
        Assertions.assertEquals("Entities", files.get(0).getName());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/include/Entities.h", files.get(0).getPath());
        Assertions.assertNull(files.get(0).getParent());
    }

    private List<Element> extractFileFromFile(String filePath) throws IOException {
        CharStream charStream = CharStreams.fromFileName(filePath);
        CPP14Lexer lexer = new CPP14Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokens);
        TranslationUnitContext ctx = parser.translationUnit();

        CppElementExtractor extractor = new CppElementExtractor();
        extractor.extract(ctx);
        return extractor.getElementManager().getFiles();
    }
    
}
