package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.cpp;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppControlExtractor;
import generated.antlr.cpp.CPP14Lexer;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14Parser.TranslationUnitContext;

public class CppControlExtractorTest {
    private final String sourcePath = "src/test/resources/cpp/interface/edu/";

    @Test
    void controlExtractorMainCPPTest() throws IOException {
        String filePath = sourcePath + "src/main.cpp";
        List<ControlElement> controls = extractControlElementsFromFile(filePath);

        Assertions.assertEquals(1, controls.size());
        Assertions.assertEquals("main()", controls.get(0).getName());
        Assertions.assertEquals("main", controls.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.FILE, controls.get(0).getParent().getType());

    }

    @Test
    void controlExtractorEntitiesCPPTest() throws IOException {
        String filePath = sourcePath + "src/Entities.cpp";
        List<ControlElement> controls = extractControlElementsFromFile(filePath);

        Assertions.assertEquals(9, controls.size());

    }

    @Test
    void controlExtractorEntitiesHTest() throws IOException {
        String filePath = sourcePath + "include/Entities.h";
        List<ControlElement> controls = extractControlElementsFromFile(filePath);

        Assertions.assertEquals(0, controls.size());
    }

    private List<ControlElement> extractControlElementsFromFile(String filePath) throws IOException {
        CharStream charStream = CharStreams.fromFileName(filePath);
        CPP14Lexer lexer = new CPP14Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokens);
        TranslationUnitContext ctx = parser.translationUnit();

        CppControlExtractor extractor = new CppControlExtractor();
        return extractor.visitTranslationUnit(ctx);
    }

}
