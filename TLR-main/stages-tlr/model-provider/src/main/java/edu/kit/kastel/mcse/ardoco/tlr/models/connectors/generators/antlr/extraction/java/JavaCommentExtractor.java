package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import generated.antlr.java.JavaLexer;

public class JavaCommentExtractor extends CommentExtractor {

    public JavaCommentExtractor(CommonTokenStream tokens, String path) {
        super(tokens, path);
    }

    @Override
    protected boolean isComment(Token token) {
        return token.getChannel() == JavaLexer.HIDDEN;
    }

    @Override
    protected boolean isValidComment(String comment) {
        return !comment.isEmpty() && ((comment.startsWith("//") || comment.startsWith("/*")));
    }

    @Override
    protected String cleanseComment(String text) {
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

}
