package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;

public class JavaCommentExtractor extends JavaParserBaseVisitor<List<CommentElement>> {
    private List<CommentElement> comments = new ArrayList<>();


    @Override 
    public List<CommentElement> visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        super.visitCompilationUnit(ctx);
        return comments;
    }
    
}
