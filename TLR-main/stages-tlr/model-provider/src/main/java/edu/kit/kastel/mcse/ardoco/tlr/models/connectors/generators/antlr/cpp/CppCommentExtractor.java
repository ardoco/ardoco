package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.cpp;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import generated.antlr.cpp.CPP14Lexer;

public class CppCommentExtractor {
    private final List<CommentElement> comments;
    private final String path;
    private final CommonTokenStream tokens;

    public CppCommentExtractor(CommonTokenStream tokens, String path) {
        this.tokens = tokens;
        this.path = path;
        this.comments = new ArrayList<>();
    }

    public List<CommentElement> getComments() {
        return this.comments;
    }

    public void extract() {
        List<Token> allTokens = tokens.getTokens();

        for (Token token : allTokens) {
            if (token.getType() == CPP14Lexer.LineComment || token.getType() == CPP14Lexer.BlockComment) {
                String text = token.getText().trim(); // Trim whitespace
                if (!text.isEmpty() && text.startsWith("//") || text.startsWith("/*")) {
                    int startLine = token.getLine();
                    int endLine = token.getLine() + countNewLines(text);
                    String cleansedText = cleanseText(text);
                    CommentElement comment = new CommentElement(cleansedText, startLine, endLine, path);
                    comments.add(comment);
                }
            }
        }
    }

    private int countNewLines(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    private String cleanseText(String text) {
        // Remove block comment delimiters (/* and */)
        text = text.replaceAll("^/\\*+|\\*+/$", "").trim();
    
        // Remove leading '*' characters from each line while keeping line breaks
        text = text.replaceAll("(?m)^\\s*\\* ?", "");
    
        // Remove '//' from the start of each line
        text = text.replaceAll("(?m)^\\s*// ?", "");
    
        // Normalize multiple newlines and spaces → Collapse multiple newlines into a single space
        text = text.replaceAll("\\s*[\r\n]+\\s*", " ");
    
        return text.trim();
    }
    
    
    
}
