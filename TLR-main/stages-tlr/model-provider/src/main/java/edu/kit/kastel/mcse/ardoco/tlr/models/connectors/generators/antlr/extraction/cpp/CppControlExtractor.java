package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14ParserBaseVisitor;

public class CppControlExtractor extends CPP14ParserBaseVisitor<List<BasicElement>> {
    List<BasicElement> controls = new ArrayList<>();

    @Override
    public List<BasicElement> visitTranslationUnit(CPP14Parser.TranslationUnitContext ctx) {
        if (ctx.declarationseq() != null) {
            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration);
            }
        }
        return controls;
    }

    @Override
    public List<BasicElement> visitDeclaration(CPP14Parser.DeclarationContext ctx) {
        if (ctx.namespaceDefinition() != null) {
            visitNamespaceDefinition(ctx.namespaceDefinition());
        }
        if (ctx.functionDefinition() != null) {
            visitFunctionDefinition(ctx.functionDefinition());
        }

        return controls;
    }

    @Override
    public List<BasicElement> visitNamespaceDefinition(CPP14Parser.NamespaceDefinitionContext ctx) {
        if (ctx.declarationseq() != null) {
            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration);
            }
        }
        return controls;
    }

    @Override
    public List<BasicElement> visitFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {
        if (ctx.declarator() == null) {
            return controls;
        }
        String name = ctx.declarator().getText();
        Parent parent = new CppParentExtractor().getParent(ctx);
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
