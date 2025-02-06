package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.PathExtractor;
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
        } else if (typeDeclarationContext.recordDeclaration() != null) {
            visitRecordDeclaration(typeDeclarationContext.recordDeclaration());
        }
    }
    return classes;
}

@Override
public List<ClassElement> visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
    String name = ctx.identifier().getText();
    String path = PathExtractor.extractPath(ctx);
    Parent parent = JavaParentExtractor.getParent(ctx);
    String extendsClass = getExtendsClass(ctx);
    List<String> implementedInterfaces = extractImplementedInterfaces(ctx);
    int fromLine = ctx.getStart().getLine();
    int toLine = ctx.getStop().getLine();

    ClassElement classElement = new ClassElement(name, path, parent, extendsClass, implementedInterfaces);
    classElement.setFromLine(fromLine);
    classElement.setToLine(toLine);
    classes.add(classElement);
    return visitChildClasses(ctx);
}

@Override 
public List<ClassElement> visitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
    String name = ctx.identifier().getText();
    Parent parent = JavaParentExtractor.getParent(ctx);
    String path = PathExtractor.extractPath(ctx);
    int fromLine = ctx.getStart().getLine();
    int toLine = ctx.getStop().getLine();

    ClassElement classElement = new ClassElement(name, path, parent);
    classElement.setFromLine(fromLine);
    classElement.setToLine(toLine);
    classes.add(classElement);
    // You cannot have inner classes/enums in an enum
    return classes;
}

@Override
public List<ClassElement> visitRecordDeclaration(JavaParser.RecordDeclarationContext ctx) {
    String name = ctx.identifier().getText();
    Parent parent = JavaParentExtractor.getParent(ctx);
    String path = PathExtractor.extractPath(ctx);
    int fromLine = ctx.getStart().getLine();
    int toLine = ctx.getStop().getLine();

    // Records can only implement Interfaces, not extend classes
    List<String> implementedInterfaces = extractImplementedInterfaces(ctx);
    ClassElement classElement = new ClassElement(name, path, parent);
    classElement.addImplementedInterfaces(implementedInterfaces);
    classElement.setFromLine(fromLine);
    classElement.setToLine(toLine);
    classes.add(classElement);
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
        if (typeListContext != null) {
            implementedInterfaces = extractImplementedInterfaces(typeListContext);
        }
    }
    return implementedInterfaces;
}

private List<String> extractImplementedInterfaces(JavaParser.RecordDeclarationContext ctx) {
    List<String> implementedInterfaces = new ArrayList<>();
    if (ctx.typeList() != null) {
        implementedInterfaces = extractImplementedInterfaces(ctx.typeList());
    }
    return implementedInterfaces;
}

private List<String> extractImplementedInterfaces(JavaParser.TypeListContext ctx) {
    List<String> implementedInterfaces = new ArrayList<>();
    if (ctx != null) {
        ctx.typeType().forEach(typeTypeContext -> {
            // Extract the text and remove generic type parameters
            String typeName = typeTypeContext.getText();
            String simpleName = typeName.replaceAll("<.*?>", ""); // Remove everything inside <>
            implementedInterfaces.add(simpleName);
        });
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
