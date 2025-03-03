package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParserBaseVisitor;

public class JavaInterfaceExtractor extends JavaParserBaseVisitor<List<InterfaceElement>> {
    private final List<InterfaceElement> interfaces = new ArrayList<>();

    @Override
    public List<InterfaceElement> visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        super.visitCompilationUnit(ctx);
        return interfaces;
    }

    @Override
    public List<InterfaceElement> visitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
        String name = ctx.identifier().getText();
        String path = PathExtractor.extractPath(ctx);
        Parent parent = new JavaParentExtractor().getParent(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        InterfaceElement interfaceElement = new InterfaceElement(name, path, parent);
        interfaceElement.setStartLine(startLine);
        interfaceElement.setEndLine(endLine);
        interfaces.add(interfaceElement);
        return interfaces;
    }
}
