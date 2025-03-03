package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ParentExtractor;
import generated.antlr.java.JavaParser;

public final class JavaParentExtractor extends ParentExtractor {

    @Override
    protected boolean isValidParent(ParserRuleContext parentCtx, String path) {
        return parentCtx instanceof JavaParser.ClassDeclarationContext
                || parentCtx instanceof JavaParser.InterfaceDeclarationContext
                || parentCtx instanceof JavaParser.MethodDeclarationContext
                || parentCtx instanceof JavaParser.CompilationUnitContext;
    }

    @Override
    protected Parent buildParent(ParserRuleContext parentCtx, String path) {
        if (parentCtx instanceof JavaParser.ClassDeclarationContext) {
            return buildParentFromClassContext((JavaParser.ClassDeclarationContext) parentCtx, path, BasicType.CLASS);
        } else if (parentCtx instanceof JavaParser.InterfaceDeclarationContext) {
            return buildParentFromInterfaceContext((JavaParser.InterfaceDeclarationContext) parentCtx, path,
                    BasicType.INTERFACE);
        } else if (parentCtx instanceof JavaParser.MethodDeclarationContext) {
            return buildParentFromControlContext((JavaParser.MethodDeclarationContext) parentCtx, path,
                    BasicType.CONTROL);
        } else if (parentCtx instanceof JavaParser.CompilationUnitContext) {
            return buildParentFromCompilationUnitContext((JavaParser.CompilationUnitContext) parentCtx, path,
                    BasicType.COMPILATIONUNIT);
        }
        return null;
    }

    private static Parent buildParentFromCompilationUnitContext(JavaParser.CompilationUnitContext ctx, String path,
            BasicType type) {
        // Find the Name of the CompilationUnit/File through the first typeDeclaration's
        // name
        for (JavaParser.TypeDeclarationContext typeDeclaration : ctx.typeDeclaration()) {
            if (typeDeclaration.classDeclaration() != null) {
                return buildParentFromClassContext(typeDeclaration.classDeclaration(), path, type);
            } else if (typeDeclaration.interfaceDeclaration() != null) {
                return buildParentFromInterfaceContext(typeDeclaration.interfaceDeclaration(), path, type);
            } else if (typeDeclaration.enumDeclaration() != null) {
                return buildParentFromEnumContext(typeDeclaration.enumDeclaration(), path, type);
            }
        }
        return null;
    }

    private static Parent buildParentFromClassContext(JavaParser.ClassDeclarationContext ctx, String path,
            BasicType type) {
        String parentName = ctx.identifier().getText();
        return new Parent(parentName, path, type);
    }

    private static Parent buildParentFromInterfaceContext(JavaParser.InterfaceDeclarationContext ctx, String path,
            BasicType type) {
        String parentName = ctx.identifier().getText();
        return new Parent(parentName, path, type);
    }

    private static Parent buildParentFromControlContext(JavaParser.MethodDeclarationContext ctx, String path,
            BasicType type) {
        String parentName = ctx.identifier().getText();
        return new Parent(parentName, path, type);
    }

    private static Parent buildParentFromEnumContext(JavaParser.EnumDeclarationContext ctx, String path,
            BasicType type) {
        String parentName = ctx.identifier().getText();
        return new Parent(parentName, path, type);
    }
}
