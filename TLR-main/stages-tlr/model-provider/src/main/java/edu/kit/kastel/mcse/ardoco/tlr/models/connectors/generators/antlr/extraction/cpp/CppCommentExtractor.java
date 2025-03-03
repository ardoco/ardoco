package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import generated.antlr.cpp.CPP14Lexer;

public class CppCommentExtractor extends CommentExtractor {

    public CppCommentExtractor(CommonTokenStream tokens, String path) {
        super(tokens, path);
    }

    @Override
    protected boolean isComment(Token token) {
        return token.getType() == CPP14Lexer.LineComment || token.getType() == CPP14Lexer.BlockComment;
    }

    @Override 
    protected boolean isValidComment(String comment) {
        return !comment.isEmpty() && (comment.startsWith("//") || comment.startsWith("/*"));
    }

    @Override
    protected String cleanseComment(String text) {
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
