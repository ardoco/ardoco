package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import generated.antlr.java.JavaLexer;

public class JavaCommentExtractor {
    private final List<CommentElement> comments;
    private final String path;
    private final CommonTokenStream tokens;

    public JavaCommentExtractor(CommonTokenStream tokens, String path) {
        this.tokens = tokens;
        this.path = path;
        this.comments = new ArrayList<>();
    }


    public void extract() {
        List<Token> allTokens = tokens.getTokens();

        for (Token token : allTokens) {
            if (token.getChannel() == JavaLexer.HIDDEN) {
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
        // Remove block comment delimiters (/** and */)
        text = text.replaceAll("^/\\*+|\\*/$", "").trim();
    
        // Remove leading '*' characters from each line but keep newlines
        text = text.replaceAll("(?m)^\\s*\\* ?", "").trim(); 
    
        // Remove inline '//' comments
        text = text.replaceAll("^//", "").trim();
    
        // Normalize spaces: Convert multiple spaces/newlines into a single space
        text = text.replaceAll("\\s+", " ");
    
        return text;
    }

    
    public List<CommentElement> getComments() {
        return this.comments;
    }
}
