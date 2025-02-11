package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.java.JavaParser;

public final class JavaParentExtractor {
    public static Parent getParent(ParserRuleContext ctx) {
        ParserRuleContext parentCtx = ctx.getParent();
        while (parentCtx != null) {
            String path = PathExtractor.extractPath(parentCtx);
            if (parentCtx instanceof JavaParser.ClassDeclarationContext) {
                JavaParser.ClassDeclarationContext classCtx = (JavaParser.ClassDeclarationContext) parentCtx;
                return buildParentFromClassContext(classCtx, path, BasicType.CLASS);
            } else if (parentCtx instanceof JavaParser.InterfaceDeclarationContext) {
                JavaParser.InterfaceDeclarationContext interfaceCtx = (JavaParser.InterfaceDeclarationContext) parentCtx;
                return buildParentFromInterfaceContext(interfaceCtx, path, BasicType.INTERFACE);
            } else if (parentCtx instanceof JavaParser.MethodDeclarationContext) {
                JavaParser.MethodDeclarationContext controlCtx = (JavaParser.MethodDeclarationContext) parentCtx;
                return buildParentFromControlContext(controlCtx, path, BasicType.CONTROL);
            } else if (parentCtx instanceof JavaParser.CompilationUnitContext) {
                JavaParser.CompilationUnitContext compilationUnitCtx = (JavaParser.CompilationUnitContext) parentCtx;
                return buildParentFromCompilationUnitContext(compilationUnitCtx, path, BasicType.COMPILATIONUNIT);
            } else parentCtx = parentCtx.getParent();
        }
        return null;
    }

    private static Parent buildParentFromCompilationUnitContext(JavaParser.CompilationUnitContext ctx, String path, BasicType type) {
        // Find the Name of the CompilationUnit/File through the first typeDeclaration's name
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


    private static Parent buildParentFromClassContext(JavaParser.ClassDeclarationContext ctx, String path, BasicType type) {
        String parentName = ctx.identifier().getText();
        return new Parent(parentName, path, type);
    }

    private static Parent buildParentFromInterfaceContext(JavaParser.InterfaceDeclarationContext ctx, String path, BasicType type) {
        String parentName = ctx.identifier().getText();
        return new Parent(parentName, path, type);
    }

    private static Parent buildParentFromControlContext(JavaParser.MethodDeclarationContext ctx, String path, BasicType type) {
        String parentName = ctx.identifier().getText();
        return new Parent(parentName, path, type);
    }

    private static Parent buildParentFromEnumContext(JavaParser.EnumDeclarationContext ctx, String path, BasicType type) {
        String parentName = ctx.identifier().getText();
        return new Parent(parentName, path, type);
    }    
}
