package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import generated.antlr.python3.Python3Lexer;

public class Python3CommentExtractor {
    private final List<CommentElement> comments;
    private final String path;
    private final CommonTokenStream tokens;

    public Python3CommentExtractor(CommonTokenStream tokens, String path) {
        this.tokens = tokens;
        this.path = path;
        this.comments = new ArrayList<>();
    }

    public void extract() {
        List<Token> allTokens = tokens.getTokens();

        for (Token token : allTokens) {
            boolean isComment = token.getType() == Python3Lexer.COMMENT && token.getText().startsWith("#");
            boolean isMultiLineComment = token.getType() == Python3Lexer.STRING && isStandaloneDocstring(token);


            if (isComment || isMultiLineComment) {
                String text = token.getText().trim(); // Trim whitespace 
                int startLine = token.getLine();
                int endLine = token.getLine() + countNewLines(text);
                
                String cleansedText = cleanseText(text);
                CommentElement comment = new CommentElement(cleansedText, startLine, endLine, path);
                comments.add(comment);
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
        // Remove single-line comment marker (#)
        text = text.replaceAll("^# ?", "").trim();
    
        // Remove multi-line comment markers (triple quotes """ or ''')
        text = text.replaceAll("^(['\"]{3})|(['\"]{3})$", "").trim();
    
        // Replace multiple newlines with a single space
        text = text.replaceAll("\\s*\\n\\s*", " "); // Removes line breaks while keeping words separated
    
        return text;
    }
    

    private boolean isStandaloneDocstring(Token token) {
        // Get actual token text
        String text = token.getText().trim();
    
        // Ensure it's a triple-quoted string
        if (!(text.startsWith("\"\"\"") || text.startsWith("'''"))) {
            return false;
        }
    
        // Check previous token to filter out normal string assignments
        int tokenIndex = token.getTokenIndex();
        if (tokenIndex > 0) {
            Token previousToken = tokens.get(tokenIndex - 1);
            if (previousToken.getText().equals("=")) {  // If assigned to a variable, it's not a comment
                return false;
            }
        }
    
        return true; // This is a standalone docstring
    }
    
    
    public List<CommentElement> getComments() {
        return this.comments;
    }
}
