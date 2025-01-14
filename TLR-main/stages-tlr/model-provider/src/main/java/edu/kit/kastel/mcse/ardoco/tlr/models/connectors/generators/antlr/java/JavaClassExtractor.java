package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import generated.antlr.JavaParser;
import generated.antlr.JavaParserBaseVisitor;

public class JavaClassExtractor extends JavaParserBaseVisitor<List<ClassElement>> {
    private final List<ClassElement> classes = new ArrayList<>();

@Override
public List<ClassElement> visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
    for (JavaParser.TypeDeclarationContext typeDeclarationContext : ctx.typeDeclaration()) {
        if (typeDeclarationContext.classDeclaration() != null) {
            visitClassDeclaration(typeDeclarationContext.classDeclaration());
        } else if (typeDeclarationContext.enumDeclaration() != null) {
            visitEnumDeclaration(typeDeclarationContext.enumDeclaration());
        }
    }
    return classes;
}

@Override
public List<ClassElement> visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
    String name = ctx.identifier().getText();
    Parent parent = JavaParentExtractor.getParent(ctx);
    String extendsClass = getExtendsClass(ctx);
    List<String> implementedInterfaces = extractImplementedInterfaces(ctx);
    ClassElement classElement = new ClassElement(name, parent, extendsClass, implementedInterfaces);
    classes.add(classElement);
    return visitChildClasses(ctx);
}

@Override 
public List<ClassElement> visitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
    String name = ctx.identifier().getText();
    Parent parent = JavaParentExtractor.getParent(ctx);
    ClassElement classElement = new ClassElement(name, parent);
    classes.add(classElement);
    // You cannot have inner classes/enums in an enum
    return classes;
}

private String getExtendsClass(JavaParser.ClassDeclarationContext ctx) {
    if (ctx.typeType() != null) {
        return ctx.typeType().getText();
    }
    return "";
}

private List<String> extractImplementedInterfaces(JavaParser.ClassDeclarationContext ctx) {
    List<String> implementedInterfaces = new ArrayList<>();
    for (JavaParser.TypeListContext typeListContext : ctx.typeList()) {
        if (typeListContext.typeType() != null) {
            typeListContext.typeType().forEach(typeTypeContext -> implementedInterfaces.add(typeTypeContext.getText()));
        }
    }
    return implementedInterfaces;
}

private List<ClassElement> visitChildClasses(JavaParser.ClassDeclarationContext ctx) {
    for (JavaParser.ClassBodyDeclarationContext child : ctx.classBody().classBodyDeclaration()) {
        if (hasInnerClasses(child)) {
            visitClassDeclaration(child.memberDeclaration().classDeclaration());
        }
    }
    return classes;
}

private boolean hasInnerClasses(JavaParser.ClassBodyDeclarationContext ctx) {
    return ctx.memberDeclaration().classDeclaration() != null;
}

}
