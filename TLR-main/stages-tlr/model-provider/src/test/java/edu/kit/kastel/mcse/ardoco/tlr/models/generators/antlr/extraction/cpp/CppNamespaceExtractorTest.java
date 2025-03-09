package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.cpp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppElementExtractor;
import generated.antlr.cpp.CPP14Lexer;


public class CppNamespaceExtractorTest {
    private final String sourcePath = "src/test/resources/cpp/interface/edu/";

    @Test
    void namespaceExtractorMainCPPTest() throws IOException {
        String filePath = sourcePath + "src/main.cpp";
        List<Element> namespaces = extractNamespaceFromFile(filePath);

        Assertions.assertEquals(0, namespaces.size());
    }

    @Test
    void namespaceExtractorEntitiesCPPTest() throws IOException {
        String filePath = sourcePath + "src/Entities.cpp";
        List<Element> namespaces = extractNamespaceFromFile(filePath);

        Assertions.assertEquals(1, namespaces.size());
        Assertions.assertEquals("Entities", namespaces.get(0).getName());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/src/Entities.cpp", namespaces.get(0).getPath());
        Assertions.assertEquals("Entities", namespaces.get(0).getParent().getName());
        Assertions.assertEquals(Type.FILE, namespaces.get(0).getParent().getType());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/src/Entities.cpp",
                namespaces.get(0).getParent().getPath());
    }

    @Test
    void namespaceExtractorEntitiesHTest() throws IOException {
        String filePath = sourcePath + "include/Entities.h";
        List<Element> namespaces = extractNamespaceFromFile(filePath);

        Assertions.assertEquals(1, namespaces.size());
        Assertions.assertEquals("Entities", namespaces.get(0).getName());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/include/Entities.h", namespaces.get(0).getPath());
        Assertions.assertEquals("Entities", namespaces.get(0).getParent().getName());
        Assertions.assertEquals(Type.FILE, namespaces.get(0).getParent().getType());
        Assertions.assertEquals("src/test/resources/cpp/interface/edu/include/Entities.h",
                namespaces.get(0).getParent().getPath());
    }

    private List<Element> extractNamespaceFromFile(String filePath) throws IOException {
        CppElementExtractor extractor = new CppElementExtractor();
        Path path = Path.of(filePath);
        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromPath(path));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        extractor.extract(tokenStream);
        return extractor.getElements().getNamespaces();
    }

}
