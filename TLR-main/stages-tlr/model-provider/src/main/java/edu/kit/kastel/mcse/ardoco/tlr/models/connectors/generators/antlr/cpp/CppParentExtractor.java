package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.cpp;

import org.antlr.v4.runtime.ParserRuleContext;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.cpp.CPP14Parser;

public final class CppParentExtractor {
    public static Parent getParent(ParserRuleContext ctx) {
        ParserRuleContext parentCtx = ctx.getParent();
        while (parentCtx != null) {
            String path = PathExtractor.extractPath(parentCtx);
            
            if (parentCtx instanceof CPP14Parser.ClassSpecifierContext) {
                return buildParentFromClassContext((CPP14Parser.ClassSpecifierContext) parentCtx, path, BasicType.CLASS);
            } else if (parentCtx instanceof CPP14Parser.NamespaceDefinitionContext) {
                return buildParentFromNamespaceContext((CPP14Parser.NamespaceDefinitionContext) parentCtx, path, BasicType.NAMESPACE);
            } else if (parentCtx instanceof CPP14Parser.FunctionDefinitionContext) {
                return buildParentFromFunctionContext((CPP14Parser.FunctionDefinitionContext) parentCtx, path, BasicType.CONTROL);
            } else if (parentCtx instanceof CPP14Parser.TranslationUnitContext) {
                return buildParentFromTranslationUnitContext((CPP14Parser.TranslationUnitContext) parentCtx, path, BasicType.COMPILATIONUNIT);
            } else {
                parentCtx = parentCtx.getParent(); // Continue traversing upwards
            }
        }
        return null;
    }

    private static Parent buildParentFromTranslationUnitContext(CPP14Parser.TranslationUnitContext ctx, String path, BasicType type) {
        // If a translation unit contains a class, struct, or namespace, use that as the parent name
        CPP14Parser.DeclarationseqContext decSeq = ctx.declarationseq();
        for (CPP14Parser.DeclarationContext dec : decSeq.declaration()) {
            if (dec.blockDeclaration() != null) {
                CPP14Parser.BlockDeclarationContext blockDec = dec.blockDeclaration();
                if (blockDec.simpleDeclaration() != null) {
                    CPP14Parser.SimpleDeclarationContext simpleDec = blockDec.simpleDeclaration();
                    if (simpleDec.declSpecifierSeq() != null) {
                        return new Parent(simpleDec.declSpecifierSeq().getText(), path, type);
                    }
                }
            }
            if (dec.namespaceDefinition() != null) {
                CPP14Parser.NamespaceDefinitionContext namespaceDef = dec.namespaceDefinition();
                return buildParentFromNamespaceContext(namespaceDef, path, type);
            }
            if (dec.functionDefinition() != null) {
                CPP14Parser.FunctionDefinitionContext functionDef = dec.functionDefinition();
                return buildParentFromFunctionContext(functionDef, path, type);
            }
        }
        return null;
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

