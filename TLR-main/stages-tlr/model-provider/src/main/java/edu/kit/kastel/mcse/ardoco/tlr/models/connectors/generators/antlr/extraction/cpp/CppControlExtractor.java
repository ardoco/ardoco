package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14ParserBaseVisitor;

public class CppControlExtractor extends CPP14ParserBaseVisitor<List<ControlElement>> {
    List<ControlElement> controls = new ArrayList<>();

    @Override
    public List<ControlElement> visitTranslationUnit(CPP14Parser.TranslationUnitContext ctx) {
        if (ctx.declarationseq() != null) {
            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration);
            }
        }
        return controls;
    }

    @Override
    public List<ControlElement> visitDeclaration(CPP14Parser.DeclarationContext ctx) {
        if (ctx.namespaceDefinition() != null) {
            visitNamespaceDefinition(ctx.namespaceDefinition());
        }
        if (ctx.functionDefinition() != null) {
            visitFunctionDefinition(ctx.functionDefinition());
        }

        return controls;
    }

    @Override
    public List<ControlElement> visitNamespaceDefinition(CPP14Parser.NamespaceDefinitionContext ctx) {
        if (ctx.declarationseq() != null) {
            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration);
            }
        }
        return controls;
    }

    @Override
    public List<ControlElement> visitFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {
        if (ctx.declarator() == null) {
            return controls;
        }
        String name = ctx.declarator().getText();
        Parent parent = new CppParentExtractor().getParent(ctx);
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
