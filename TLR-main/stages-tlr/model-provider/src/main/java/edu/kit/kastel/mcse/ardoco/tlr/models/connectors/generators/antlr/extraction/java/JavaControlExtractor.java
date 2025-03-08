package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParserBaseVisitor;

public class JavaControlExtractor extends JavaParserBaseVisitor<List<BasicElement>> {
    private final List<BasicElement> controls = new ArrayList<>();

    @Override
    public List<BasicElement> visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        super.visitCompilationUnit(ctx);
        return controls;
    }

    @Override
    public List<BasicElement> visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        String name = ctx.identifier().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        BasicElement BasicElement = new BasicElement(name, path, parent);
        BasicElement.setStartLine(startLine);
        BasicElement.setEndLine(endLine);
        controls.add(BasicElement);
        return controls;
    }
}