package edu.kit.kastel.mcse.ardoco.tlr.models.generators.antlr.python3;

import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3.Python3CommentExtractor;
import generated.antlr.python3.Python3Lexer;
import generated.antlr.python3.Python3Parser;

public class Python3CommentExtractorTest {
    private final String sourcePath = "src/test/resources/python/interface/edu/";

    @Test
    void commentExtractorAPyClassTest() throws IOException {
        String filePath = sourcePath + "APyClass.py";
        List<CommentElement> comments = extractCommentsFromFile(filePath);
        Assertions.assertEquals(4, comments.size());

        // Detailed Assertions
        Assertions.assertEquals(1, comments.get(0).getStartLine());
        Assertions.assertEquals(1, comments.get(0).getEndLine());
        Assertions.assertEquals("This is a Comment for AClass", comments.get(0).getText());

        Assertions.assertEquals(3, comments.get(1).getStartLine());
        Assertions.assertEquals(3, comments.get(1).getEndLine());
        Assertions.assertEquals("This is an inline comment for class_variable of AClass", comments.get(1).getText());

        Assertions.assertEquals(15, comments.get(2).getStartLine());
        Assertions.assertEquals(18, comments.get(2).getEndLine());
        Assertions.assertEquals("This is a multiple line comment for InnerClass1", comments.get(2).getText());

        Assertions.assertEquals(31, comments.get(3).getStartLine());
        Assertions.assertEquals(34, comments.get(3).getEndLine());
        Assertions.assertEquals("This is a multiple line comment for InnerClass2", comments.get(3).getText());
    }

    private List<CommentElement> extractCommentsFromFile(String filePath) throws IOException{
        CharStream input = CharStreams.fromFileName(filePath);
        Python3Lexer lexer = new Python3Lexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        parser.file_input();

        Python3CommentExtractor extractor = new Python3CommentExtractor(tokens, filePath);
        extractor.extract();
        return extractor.getComments();
    }

    
}
