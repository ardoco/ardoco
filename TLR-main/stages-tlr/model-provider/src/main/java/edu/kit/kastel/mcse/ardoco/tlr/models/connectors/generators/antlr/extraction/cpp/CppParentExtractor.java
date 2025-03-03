package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ParentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.cpp.CPP14Parser;

public final class CppParentExtractor extends ParentExtractor{

    @Override
    protected boolean isValidParent(ParserRuleContext parentCtx, String path) {
        return parentCtx instanceof CPP14Parser.ClassSpecifierContext
                || parentCtx instanceof CPP14Parser.NamespaceDefinitionContext
                || parentCtx instanceof CPP14Parser.FunctionDefinitionContext
                || parentCtx instanceof CPP14Parser.TranslationUnitContext;
    }

    @Override
    protected Parent buildParent(ParserRuleContext parentCtx, String path) {
        if (parentCtx instanceof CPP14Parser.ClassSpecifierContext) {
            return buildParentFromClassContext((CPP14Parser.ClassSpecifierContext) parentCtx, path, BasicType.CLASS);
        } else if (parentCtx instanceof CPP14Parser.NamespaceDefinitionContext) {
            return buildParentFromNamespaceContext((CPP14Parser.NamespaceDefinitionContext) parentCtx, path, BasicType.NAMESPACE);
        } else if (parentCtx instanceof CPP14Parser.FunctionDefinitionContext) {
            return buildParentFromFunctionContext((CPP14Parser.FunctionDefinitionContext) parentCtx, path, BasicType.CONTROL);
        } else if (parentCtx instanceof CPP14Parser.TranslationUnitContext) {
            return buildParentFromTranslationUnitContext((CPP14Parser.TranslationUnitContext) parentCtx, path, BasicType.FILE);
        }
        return null;
    }

    private static Parent buildParentFromTranslationUnitContext(CPP14Parser.TranslationUnitContext ctx, String path, BasicType type) {
        // Use file name as parent name
        String parentName = PathExtractor.extractNameFromPath(ctx);
        return new Parent(parentName, path, type);
    }

    private static Parent buildParentFromClassContext(CPP14Parser.ClassSpecifierContext ctx, String path, BasicType type) {
        String parentName = ctx.classHead().classHeadName().getText();
        return new Parent(parentName, path, type);
    }


    private static Parent buildParentFromNamespaceContext(CPP14Parser.NamespaceDefinitionContext ctx, String path, BasicType type) {
        String parentName = ctx.Identifier().getText();
        return new Parent(parentName, path, type);
    }

    private static Parent buildParentFromFunctionContext(CPP14Parser.FunctionDefinitionContext ctx, String path, BasicType type) {
        String parentName = ctx.declarator().getText();
        return new Parent(parentName, path, type);
    }
}

