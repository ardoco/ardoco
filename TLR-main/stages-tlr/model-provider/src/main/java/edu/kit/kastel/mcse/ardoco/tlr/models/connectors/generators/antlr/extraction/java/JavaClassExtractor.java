package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParserBaseVisitor;

public class JavaClassExtractor extends JavaParserBaseVisitor<List<JavaClassElement>> {
    private final List<JavaClassElement> classes = new ArrayList<>();

    @Override
    public List<JavaClassElement> visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
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
    public List<JavaClassElement> visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        String name = ctx.identifier().getText();
        String path = PathExtractor.extractPath(ctx);
        Parent parent = new JavaParentExtractor().getParent(ctx);
        String extendsClass = getExtendsClass(ctx);
        List<String> implementedInterfaces = extractImplementedInterfaces(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        JavaClassElement classElement = new JavaClassElement(name, path, parent, extendsClass, implementedInterfaces);
        classElement.setStartLine(startLine);
        classElement.setEndLine(endLine);
        classes.add(classElement);
        return visitChildClasses(ctx);
    }

    @Override
    public List<JavaClassElement> visitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
        String name = ctx.identifier().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        JavaClassElement classElement = new JavaClassElement(name, path, parent);
        classElement.setStartLine(startLine);
        classElement.setEndLine(endLine);
        classes.add(classElement);
        // You cannot have inner classes/enums in an enum
        return classes;
    }

    @Override
    public List<JavaClassElement> visitRecordDeclaration(JavaParser.RecordDeclarationContext ctx) {
        String name = ctx.identifier().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        // Records can only implement Interfaces, not extend classes
        List<String> implementedInterfaces = extractImplementedInterfaces(ctx);
        JavaClassElement classElement = new JavaClassElement(name, path, parent);
        classElement.addImplementedInterfaces(implementedInterfaces);
        classElement.setStartLine(startLine);
        classElement.setEndLine(endLine);
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

    private List<JavaClassElement> visitChildClasses(JavaParser.ClassDeclarationContext ctx) {
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
