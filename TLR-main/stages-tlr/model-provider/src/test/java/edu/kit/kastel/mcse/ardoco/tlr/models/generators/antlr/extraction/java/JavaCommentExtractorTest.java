package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.extraction.java;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java.JavaCommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementManager;
import generated.antlr.java.JavaLexer;
import generated.antlr.java.JavaParser;

public class JavaCommentExtractorTest {
    private final String sourcePath = "src/test/resources/interface/edu/";

    @Test
    void commentExtractorAClassTest() throws IOException {
        String filePath = sourcePath + "AClass.java";
        List<Comment> comments = extractCommentsFromFile(filePath);
        Assertions.assertEquals(3, comments.size());
        Assertions.assertEquals(5, comments.get(0).getStartLine());
        Assertions.assertEquals(7, comments.get(0).getEndLine());
        Assertions.assertEquals("This is a Test Java Doc Comment", comments.get(0).getText());

        Assertions.assertEquals(17, comments.get(1).getStartLine());
        Assertions.assertEquals(17, comments.get(1).getEndLine());
        Assertions.assertEquals("This is a Test Line Comment", comments.get(1).getText());

        Assertions.assertEquals(20, comments.get(2).getStartLine());
        Assertions.assertEquals(27, comments.get(2).getEndLine());
        Assertions.assertEquals("This is a Test Block Comment", comments.get(2).getText());
    }

    @Test
    void commentExtractorSuperclassTest() throws IOException {
        String filePath = sourcePath + "Superclass.java";
        List<Comment> comments = extractCommentsFromFile(filePath);
        Assertions.assertEquals(1, comments.size());
        Assertions.assertEquals(3, comments.get(0).getStartLine());
        Assertions.assertEquals(6, comments.get(0).getEndLine());
        Assertions.assertEquals("This is a Test Java Doc Comment over multiple lines", comments.get(0).getText());

    }

    @Test
    void commentExtractorAnEnumTest() throws IOException {
        String filePath = sourcePath + "AnEnum.java";
        List<Comment> comments = extractCommentsFromFile(filePath);
        Assertions.assertEquals(0, comments.size());
    }

    @Test
    void commentExtractorAnInterfaceTest() throws IOException {
        String filePath = sourcePath + "AnInterface.java";
        List<Comment> comments = extractCommentsFromFile(filePath);
        Assertions.assertEquals(0, comments.size());
    }

    @Test
    void commentExtractorExtendedInterfaceTest() throws IOException {
        String filePath = sourcePath + "ExtendedInterface.java";
        List<Comment> comments = extractCommentsFromFile(filePath);
        Assertions.assertEquals(0, comments.size());
    }

    @Test
    void commentExtractorZweiOtherInterfaceTest() throws IOException {
        String filePath = sourcePath + "zwei/OtherInterface.java";
        List<Comment> comments = extractCommentsFromFile(filePath);
        Assertions.assertEquals(0, comments.size());
    }

    @Test
    void commentExtractorDreiOtherInterfaceTest() throws IOException {
        String filePath = sourcePath + "drei/OtherInterface.java";
        List<Comment> comments = extractCommentsFromFile(filePath);
        Assertions.assertEquals(0, comments.size());
    }

    private List<Comment> extractCommentsFromFile(String filePath) throws IOException {
        CharStream input = CharStreams.fromFileName(filePath);
        JavaLexer lexer = new JavaLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        parser.compilationUnit();

        JavaElementManager manager = new JavaElementManager();
        JavaCommentExtractor extractor = new JavaCommentExtractor(manager);
        extractor.extract(filePath, tokens);
        return extractor.getCurrentComments();
    }

}
