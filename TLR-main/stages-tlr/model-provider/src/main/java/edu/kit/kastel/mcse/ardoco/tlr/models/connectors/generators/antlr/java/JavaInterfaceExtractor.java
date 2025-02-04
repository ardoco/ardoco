package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;

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
        Parent parent = JavaParentExtractor.getParent(ctx);
        InterfaceElement interfaceElement = new InterfaceElement(name, path, parent);
        interfaces.add(interfaceElement);
        return interfaces;
    }    
}
