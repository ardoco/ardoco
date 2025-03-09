package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.cpp;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp.CppCommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementManager;
import generated.antlr.cpp.CPP14Lexer;
import generated.antlr.cpp.CPP14Parser;

public class CppCommentExtractorTest {
    private final String sourcePath = "src/test/resources/cpp/interface/edu/";

    @Test
    public void commentExtractorMainCPPTest() throws IOException {
        String filePath = sourcePath + "src/main.cpp";
        List<Comment> comments = extractCommentsFromFile(filePath);

        Assertions.assertEquals(4, comments.size());
        Assertions.assertEquals(1, comments.get(0).getStartLine());
        Assertions.assertEquals(5, comments.get(0).getEndLine());
        Assertions.assertEquals(
                "Simple C++ Project Author: Your Name Description: A basic C++ project with a simple structure.",
                comments.get(0).getText());

        Assertions.assertEquals(7, comments.get(1).getStartLine());
        Assertions.assertEquals(7, comments.get(1).getEndLine());
        Assertions.assertEquals("main.cpp", comments.get(1).getText());

        Assertions.assertEquals(12, comments.get(2).getStartLine());
        Assertions.assertEquals(12, comments.get(2).getEndLine());
        Assertions.assertEquals("Create a Car object", comments.get(2).getText());

        Assertions.assertEquals(16, comments.get(3).getStartLine());
        Assertions.assertEquals(16, comments.get(3).getEndLine());
        Assertions.assertEquals("Create a Person object", comments.get(3).getText());
    }

    @Test
    public void commentExtractorEntitiesCPPTest() throws IOException {
        String filePath = sourcePath + "src/Entities.cpp";
        List<Comment> comments = extractCommentsFromFile(filePath);

        Assertions.assertEquals(1, comments.size());

        Assertions.assertEquals(2, comments.get(0).getStartLine());
        Assertions.assertEquals(2, comments.get(0).getEndLine());
        Assertions.assertEquals("Entities.cpp", comments.get(0).getText());
    }

    @Test
    public void commentExtractorEntitiesHTest() throws IOException {
        String filePath = sourcePath + "include/Entities.h";
        List<Comment> comments = extractCommentsFromFile(filePath);

        Assertions.assertEquals(2, comments.size());

        Assertions.assertEquals(1, comments.get(0).getStartLine());
        Assertions.assertEquals(1, comments.get(0).getEndLine());
        Assertions.assertEquals("Entities.h", comments.get(0).getText());

        Assertions.assertEquals(49, comments.get(1).getStartLine());
        Assertions.assertEquals(49, comments.get(1).getEndLine());
        Assertions.assertEquals("ENTITIES_H", comments.get(1).getText());
    }

    private List<Comment> extractCommentsFromFile(String filePath) throws IOException {
        CharStream charStream = CharStreams.fromFileName(filePath);
        CPP14Lexer lexer = new CPP14Lexer(charStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokens);
        parser.translationUnit();

        CppElementManager manager = new CppElementManager();
        CppCommentExtractor extractor = new CppCommentExtractor(manager);
        extractor.extract(filePath, tokens);
        return extractor.getCurrentComments();
    }
}
