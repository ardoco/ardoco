package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorageRegistry;

/**
 * Is responsible for extracting comments from an ANTLR token stream. The
 * comments validity is determined by the
 * implementing class. The extracted comments are then stored in a List of
 * Comment objects.
 */
public abstract class CommentExtractor {
    private final ElementStorageRegistry elementRegistry;
    private List<Comment> currentComments;

    protected CommentExtractor(ElementStorageRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
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
        elementRegistry.addComments(currentComments);
    }

    public List<Comment> getCurrentComments() {
        return this.currentComments;
    }

    protected abstract boolean isComment(Token token);

    protected abstract boolean isValidComment(String text);

    protected abstract String cleanseComment(String comment);

    private Comment createCommentElement(String text, int startLine, int endLine, String path) {
        return new Comment(text, startLine, endLine, path);
    }

    private int countCommentLines(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

}
