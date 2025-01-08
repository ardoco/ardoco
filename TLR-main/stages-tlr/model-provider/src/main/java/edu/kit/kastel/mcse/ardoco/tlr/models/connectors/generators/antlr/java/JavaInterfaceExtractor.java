package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;

public class JavaInterfaceExtractor extends JavaParserBaseVisitor<InterfaceElement> {

    @Override
    public InterfaceElement visitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
        String name = ctx.identifier().getText();
        Parent parent = JavaParentExtractor.getParent(ctx);
        String packageName = getPackageName(ctx);
        return new InterfaceElement(name, parent, packageName);
    }

    private static String getPackageName(JavaParser.InterfaceDeclarationContext ctx) {
    ParserRuleContext packageHolder = ctx;
    while (packageHolder != null && !(packageHolder instanceof JavaParser.CompilationUnitContext)) {
        packageHolder = packageHolder.getParent();
    }

    if (packageHolder != null) {
        JavaParser.CompilationUnitContext compilationUnit = (JavaParser.CompilationUnitContext) packageHolder;
        return compilationUnit.packageDeclaration().qualifiedName().getText();
    }
    return null;
    }

    
}
