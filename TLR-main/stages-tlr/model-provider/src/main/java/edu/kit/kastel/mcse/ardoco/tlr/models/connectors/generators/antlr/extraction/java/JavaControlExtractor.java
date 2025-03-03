package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParserBaseVisitor;

public class JavaControlExtractor extends JavaParserBaseVisitor<List<ControlElement>> {
    private final List<ControlElement> controls = new ArrayList<>();

    @Override
    public List<ControlElement> visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        super.visitCompilationUnit(ctx);
        return controls;
    }

    @Override
    public List<ControlElement> visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        String name = ctx.identifier().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        ControlElement controlElement = new ControlElement(name, path, parent);
        controlElement.setStartLine(startLine);
        controlElement.setEndLine(endLine);
        controls.add(controlElement);
        return controls;
    }
}