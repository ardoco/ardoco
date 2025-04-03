package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3;

import org.antlr.v4.runtime.Token;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3.Python3ElementStorageRegistry;
import generated.antlr.python3.Python3Lexer;

/**
 * Responsible for extracting comments from Python3 code. Defines the rules for
 * valid comments and how to get only the text of the comment without the
 * delimiters.
 */
public class Python3CommentExtractor extends CommentExtractor {

    public Python3CommentExtractor(Python3ElementStorageRegistry elementRegistry) {
        super(elementRegistry);
    }

    @Override
    protected boolean isComment(Token token) {
        return token.getType() == Python3Lexer.COMMENT || token.getType() == Python3Lexer.STRING;
    }

    @Override
    protected boolean isValidComment(String text) {
        return !text.isEmpty() && ((text.startsWith("#") || text.startsWith("\"\"\"") || text.startsWith("'''")));
    }

    @Override
    protected String cleanseComment(String comment) {
        // Remove single-line comment marker (#)
        comment = comment.replaceAll("^# ?", "").trim();

        // Remove multi-line comment markers (triple quotes """ or ''')
        comment = comment.replaceAll("^(['\"]{3})|(['\"]{3})$", "").trim();

        // Replace multiple newlines with a single space
        comment = comment.replaceAll("\\s*\\n\\s*", " "); // Removes line breaks while keeping words separated

        return comment;
    }
}
