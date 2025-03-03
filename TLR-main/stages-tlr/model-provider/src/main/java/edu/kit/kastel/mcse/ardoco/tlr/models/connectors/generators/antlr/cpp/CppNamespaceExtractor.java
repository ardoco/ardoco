package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.cpp;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp.NamespaceElement;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14ParserBaseVisitor;

public class CppNamespaceExtractor extends CPP14ParserBaseVisitor<List<NamespaceElement>> {
    List<NamespaceElement> namespaces = new ArrayList<>();

    @Override 
    public List<NamespaceElement> visitTranslationUnit(CPP14Parser.TranslationUnitContext ctx) {
        if (ctx.declarationseq()!= null) {
            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration);
            }
        }
        return namespaces;
    }

    @Override
    public List<NamespaceElement> visitDeclaration(CPP14Parser.DeclarationContext ctx) {
        if (ctx.namespaceDefinition() != null) {
            visitNamespaceDefinition(ctx.namespaceDefinition());
        }
        return namespaces;
    }

    @Override
    public List<NamespaceElement> visitNamespaceDefinition(CPP14Parser.NamespaceDefinitionContext ctx) {
        if (ctx.declarationseq() != null) {
            if (ctx.Namespace() != null) {
                String name = "anonymous";
                if (ctx.Identifier().getText() != null) {
                    name = ctx.Identifier().getText();
                }
                String path = PathExtractor.extractPath(ctx);
                Parent parent = CppParentExtractor.getParent(ctx);
                int startLine = ctx.getStart().getLine();
                int endLine = ctx.getStop().getLine();
                NamespaceElement namespaceElement = new NamespaceElement(name, path, parent);
                namespaceElement.setStartLine(startLine);
                namespaceElement.setEndLine(endLine);
                namespaces.add(namespaceElement);
            }
            for (CPP14Parser.DeclarationContext declaration : ctx.declarationseq().declaration()) {
                visitDeclaration(declaration);
            }
        }
        return namespaces;
    }    
}
