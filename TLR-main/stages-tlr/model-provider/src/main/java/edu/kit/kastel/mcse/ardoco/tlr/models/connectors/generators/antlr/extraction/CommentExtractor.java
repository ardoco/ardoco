package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;

public abstract class CommentExtractor {
    private final List<CommentElement> comments;
    private final String path;
    protected final CommonTokenStream tokens;
    

    protected CommentExtractor(CommonTokenStream tokens, String path) {
        this.tokens = tokens;
        this.path = path;
        this.comments = new ArrayList<>();
    }

    public void extract() {
        List<Token> allTokens = tokens.getTokens();
        
        for (Token token : allTokens) {
            if (isComment(token)) {
                String text = token.getText().trim();
                if (isValidComment(text)) {
                    int startLine = token.getLine();
                    int endLine = token.getLine() + countCommentLines(text);
                    String cleansedText = cleanseComment(text);
                    CommentElement comment = createCommentElement(cleansedText, startLine, endLine);
                    comments.add(comment);
                }
            }
        }
    }

    protected abstract boolean isComment(Token token);
    protected abstract boolean isValidComment(String text);
    protected abstract String cleanseComment(String comment);

    public List<CommentElement> getComments() {
        return this.comments;
    }

    protected CommentElement createCommentElement(String text, int startLine, int endLine) {
        return new CommentElement(text, startLine, endLine, path);
    }

    protected int countCommentLines(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }


    
}
