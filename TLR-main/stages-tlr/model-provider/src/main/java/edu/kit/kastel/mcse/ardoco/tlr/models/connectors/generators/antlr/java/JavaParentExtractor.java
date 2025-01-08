package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.JavaParser;

public final class JavaParentExtractor {
    public static Parent getParent(ParserRuleContext ctx) {
        ParserRuleContext parentCtx = ctx.getParent();
        while (parentCtx != null) {
            if (parentCtx instanceof JavaParser.ClassDeclarationContext) {
                String parentName = ((JavaParser.ClassDeclarationContext) parentCtx).identifier().getText();
                return new Parent(parentName, BasicType.CLASS);
            } else if (parentCtx instanceof JavaParser.InterfaceDeclarationContext) {
                String parentName = ((JavaParser.InterfaceDeclarationContext) parentCtx).identifier().getText();
                return new Parent(parentName, BasicType.INTERFACE);
            } else if (parentCtx instanceof JavaParser.MethodDeclarationContext) {
                String parentName = ((JavaParser.MethodDeclarationContext) parentCtx).identifier().getText();
                return new Parent(parentName, BasicType.CONTROL);
            } else parentCtx = parentCtx.getParent();
        }
        return null;
    }
    
}
