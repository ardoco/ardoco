package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.cpp;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppNamespaceExtractor;
import generated.antlr.cpp.CPP14Lexer;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14Parser.TranslationUnitContext;

public class CppNamespaceExtractorTest {
    private final String sourcePath = "src/test/resources/cpp/interface/edu/";

    @Test
    void namespaceExtractorMainCPPTest() throws IOException {
        String filePath = sourcePath + "src/main.cpp";
        List<BasicElement> namespaces = extractNamespaceFromFile(filePath);

        Assertions.assertEquals(0, namespaces.size());
    }

    @Test
    void namespaceExtractorEntitiesCPPTest() throws IOException {
        String filePath = sourcePath + "src/Entities.cpp";
        List<BasicElement> namespaces = extractNamespaceFromFile(filePath);

        Assertions.assertEquals(1, namespaces.size());
        Assertions.assertEquals("Entities", namespaces.get(0).getName());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/src/Entities.cpp", namespaces.get(0).getPath());
        Assertions.assertEquals("Entities", namespaces.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.FILE, namespaces.get(0).getParent().getType());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/src/Entities.cpp",
                namespaces.get(0).getParent().getPath());
    }

    @Test
    void namespaceExtractorEntitiesHTest() throws IOException {
        String filePath = sourcePath + "include/Entities.h";
        List<BasicElement> namespaces = extractNamespaceFromFile(filePath);

        Assertions.assertEquals(1, namespaces.size());
        Assertions.assertEquals("Entities", namespaces.get(0).getName());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/include/Entities.h", namespaces.get(0).getPath());
        Assertions.assertEquals("Entities", namespaces.get(0).getParent().getName());
        Assertions.assertEquals(BasicType.FILE, namespaces.get(0).getParent().getType());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/include/Entities.h",
                namespaces.get(0).getParent().getPath());
    }

    private List<BasicElement> extractNamespaceFromFile(String filePath) throws IOException {
        CharStream charStream = CharStreams.fromFileName(filePath);
        CPP14Lexer lexer = new CPP14Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokens);
        TranslationUnitContext ctx = parser.translationUnit();

        CppNamespaceExtractor extractor = new CppNamespaceExtractor();
        return extractor.visitTranslationUnit(ctx);
    }

}
