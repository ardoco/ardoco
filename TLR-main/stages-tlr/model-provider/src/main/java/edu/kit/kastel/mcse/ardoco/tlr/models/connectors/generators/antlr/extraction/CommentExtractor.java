package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementManager;

public abstract class CommentExtractor {
    private final ElementManager elementManager;
    private List<Comment> currentComments;

    protected CommentExtractor(ElementManager elementManager) {
        this.elementManager = elementManager;
    }

    public void extract(String path, CommonTokenStream tokens) {
        List<Token> allTokens = tokens.getTokens();
        this.currentComments = new ArrayList<>();

        for (Token token : allTokens) {
            if (isComment(token)) {
                String text = token.getText().trim();
                if (isValidComment(text)) {
                    int startLine = token.getLine();
                    int endLine = token.getLine() + countCommentLines(text);
                    String cleansedText = cleanseComment(text);
                    Comment comment = createCommentElement(cleansedText, startLine, endLine, path);
                    this.currentComments.add(comment);
                }
            }
        }
        elementManager.addComments(currentComments);
    }

    public List<Comment> getCurrentComments() {
        return this.currentComments;
    }

    protected abstract boolean isComment(Token token);

    protected abstract boolean isValidComment(String text);

    protected abstract String cleanseComment(String comment);

    protected Comment createCommentElement(String text, int startLine, int endLine, String path) {
        return new Comment(text, startLine, endLine, path);
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
