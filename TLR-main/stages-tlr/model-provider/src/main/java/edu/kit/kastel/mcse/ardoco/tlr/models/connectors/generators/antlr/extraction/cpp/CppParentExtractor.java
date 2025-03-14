package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ParentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.cpp.CPP14Parser;

public final class CppParentExtractor extends ParentExtractor {

    @Override
    protected boolean isValidParent(ParserRuleContext parentCtx, String path) {
        return parentCtx instanceof CPP14Parser.ClassSpecifierContext
                || parentCtx instanceof CPP14Parser.NamespaceDefinitionContext
                || parentCtx instanceof CPP14Parser.FunctionDefinitionContext
                || parentCtx instanceof CPP14Parser.TranslationUnitContext;
    }

    @Override
    protected ElementIdentifier buildParent(ParserRuleContext parentCtx, String path) {
        if (parentCtx instanceof CPP14Parser.ClassSpecifierContext) {
            return buildParentFromClassContext((CPP14Parser.ClassSpecifierContext) parentCtx, path, Type.CLASS);
        } else if (parentCtx instanceof CPP14Parser.NamespaceDefinitionContext) {
            return buildParentFromNamespaceContext((CPP14Parser.NamespaceDefinitionContext) parentCtx, path,
                    Type.NAMESPACE);
        } else if (parentCtx instanceof CPP14Parser.FunctionDefinitionContext) {
            return buildParentFromFunctionContext((CPP14Parser.FunctionDefinitionContext) parentCtx, path,
                    Type.FUNCTION);
        } else if (parentCtx instanceof CPP14Parser.TranslationUnitContext) {
            return buildParentFromTranslationUnitContext((CPP14Parser.TranslationUnitContext) parentCtx, path,
                    Type.FILE);
        }
        return null;
    }

    private static ElementIdentifier buildParentFromTranslationUnitContext(CPP14Parser.TranslationUnitContext ctx, String path,
            Type type) {
        // Use file name as parent name
        String parentName = PathExtractor.extractNameFromPath(ctx);
        return new ElementIdentifier(parentName, path, type);
    }

    private static ElementIdentifier buildParentFromClassContext(CPP14Parser.ClassSpecifierContext ctx, String path,
            Type type) {
        String parentName = "anonymous";
        if (ctx.classHead().classHeadName() != null) {
            parentName = ctx.classHead().classHeadName().getText();
        } else if (ctx.classHead().classVirtSpecifier() != null) {
            parentName = ctx.classHead().classVirtSpecifier().getText();
        }
        return new Parent(parentName, path, type);
    }

    private static ElementIdentifier buildParentFromNamespaceContext(CPP14Parser.NamespaceDefinitionContext ctx, String path,
            Type type) {
        String parentName = "anonymous";
        if (ctx.Identifier() != null) {
            parentName = ctx.Identifier().getText();
        } else if (ctx.originalNamespaceName() != null) {
            parentName = ctx.originalNamespaceName().getText();
        }
        return new Parent(parentName, path, type);
    }

    private static ElementIdentifier buildParentFromFunctionContext(CPP14Parser.FunctionDefinitionContext ctx, String path,
            Type type) {
        String parentName = ctx.declarator().getText();
        return new ElementIdentifier(parentName, path, type);
    }
}
