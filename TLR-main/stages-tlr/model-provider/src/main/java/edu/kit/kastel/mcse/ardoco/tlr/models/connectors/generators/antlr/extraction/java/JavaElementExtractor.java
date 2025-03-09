package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ElementExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementManager;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParserBaseVisitor;
import generated.antlr.java.JavaParser.CompilationUnitContext;

public class JavaElementExtractor extends JavaParserBaseVisitor<Void> implements ElementExtractor {
    private final JavaElementManager elementManager;

    public JavaElementExtractor() {
        this.elementManager = new JavaElementManager();
    }

    public JavaElementExtractor(JavaElementManager elementManager) {
        this.elementManager = elementManager;
    }

    @Override
    public JavaElementManager getElements() {
        return elementManager;
    }

    @Override
    public void extract(CommonTokenStream tokens) {
        CompilationUnitContext ctx = buildContext(tokens);
        visitCompilationUnit(ctx);

    }

    private CompilationUnitContext buildContext(CommonTokenStream tokenStream) {
        JavaParser parser = new JavaParser(tokenStream);
        return parser.compilationUnit();
    }

    @Override
    public Void visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        super.visitCompilationUnit(ctx);
        for (JavaParser.TypeDeclarationContext typeDeclarationContext : ctx.typeDeclaration()) {
            if (typeDeclarationContext.classDeclaration() != null) {
                visitClassDeclaration(typeDeclarationContext.classDeclaration());
            } else if (typeDeclarationContext.enumDeclaration() != null) {
                visitEnumDeclaration(typeDeclarationContext.enumDeclaration());
            } else if (typeDeclarationContext.recordDeclaration() != null) {
                visitRecordDeclaration(typeDeclarationContext.recordDeclaration());
            }
        }
        addCompilationUnit(ctx);
        return null;
    }

    @Override
    public Void visitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        super.visitClassDeclaration(ctx);
        String name = ctx.identifier().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        String extendsClass = getExtendsClass(ctx);
        List<String> implementedInterfaces = extractImplementedInterfaces(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addClass(name, path, parent, extendsClass, implementedInterfaces, startLine, endLine);
        return null;
    }

    @Override
    public Void visitEnumDeclaration(JavaParser.EnumDeclarationContext ctx) {
        super.visitEnumDeclaration(ctx);
        String name = ctx.identifier().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addClass(name, path, parent, startLine, endLine);
        return null;
    }

    @Override
    public Void visitRecordDeclaration(JavaParser.RecordDeclarationContext ctx) {
        super.visitRecordDeclaration(ctx);
        String name = ctx.identifier().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        // Records can only implement Interfaces, not extend classes
        List<String> implementedInterfaces = extractImplementedInterfaces(ctx);
        addClass(name, path, parent, "", implementedInterfaces, startLine, endLine);
        return null;
    }

    @Override
    public Void visitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
        super.visitInterfaceDeclaration(ctx);
        String name = ctx.identifier().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addInterface(name, path, parent, startLine, endLine);
        return null;
    }

    @Override
    public Void visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
        super.visitMethodDeclaration(ctx);
        String name = ctx.identifier().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addFunction(name, path, parent, startLine, endLine);
        return null;
    }

    @Override
    public Void visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
        super.visitFieldDeclaration(ctx);
        String variableType = ctx.typeType().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        List<String> varNames = extractVariableNames(ctx.variableDeclarators().variableDeclarator());
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();
        addVariables(varNames, path, variableType, parent, startLine, endLine);
        return null;
    }

    @Override
    public Void visitLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
        super.visitLocalVariableDeclaration(ctx);
        String variableType = ctx.typeType().getText();
        Parent parent = new JavaParentExtractor().getParent(ctx);
        List<String> varNames = extractVariableNames(ctx.variableDeclarators().variableDeclarator());
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addVariables(varNames, path, variableType, parent, startLine, endLine);
        return null;
    }

    private List<String> extractVariableNames(List<JavaParser.VariableDeclaratorContext> variableDeclarators) {
        List<String> variableNames = new ArrayList<>();
        for (JavaParser.VariableDeclaratorContext variableDeclarator : variableDeclarators) {
            String name = variableDeclarator.variableDeclaratorId().identifier().getText();
            variableNames.add(name);
        }
        return variableNames;
    }

    private void addVariables(List<String> varNames, String path, String variableType, Parent parent,
            int startLine, int endLine) {
        for (String variableName : varNames) {
            addVariable(variableName, path, variableType, parent, startLine, endLine);
        }
    }

    private void addVariable(String variableName, String path, String variableType, Parent parent,
            int startLine, int endLine) {
        VariableElement variable = new VariableElement(variableName, path, variableType, parent);
        variable.setStartLine(startLine);
        variable.setEndLine(endLine);
        elementManager.addVariable(variable);
    }

    private void addClass(String name, String path, Parent parent, String extendsClass,
            List<String> implementedInterfaces,
            int startLine, int endLine) {
        JavaClassElement classElement = new JavaClassElement(name, path, parent, extendsClass,
                implementedInterfaces, startLine, endLine);
        elementManager.addClass(classElement);

    }

    private void addClass(String name, String path, Parent parent, int startLine, int endLine) {
        JavaClassElement classElement = new JavaClassElement(name, path, parent, startLine, endLine);
        elementManager.addClass(classElement);
    }

    private void addFunction(String name, String path, Parent parent, int startLine, int endLine) {
        Element method = new Element(name, path, parent, startLine, endLine);
        elementManager.addFunction(method);
    }

    private void addInterface(String name, String path, Parent parent,
            int startLine, int endLine) {
        Element interfaceElement = new Element(name, path, parent, startLine, endLine);
        elementManager.addInterface(interfaceElement);
    }

    private void addCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        Element compilationUnit = null;
        String path = PathExtractor.extractPath(ctx);
        String name = PathExtractor.extractNameFromPath(ctx);
        if (ctx.packageDeclaration() != null) {
            String packageName = ctx.packageDeclaration().qualifiedName().getText();
            String packagePath = path.substring(0, path.lastIndexOf("/") + 1);
            addPackage(packageName, packagePath);
            Parent parent = new Parent(packageName, packagePath, Type.PACKAGE);
            compilationUnit = new Element(name, path, parent);
        } else {
            compilationUnit = new Element(name, path);
        }
        elementManager.addCompilationUnit(compilationUnit);
    }

    private void addPackage(String packageName, String packagePath) {
        List<PackageElement> packageElements = elementManager.getPackages();
        String closestParentName = "";
        String closestParentPath = "";
        for (PackageElement packageElement : packageElements) {
            String existingPackagePath = packageElement.getPath();
            if (packagePath.startsWith(existingPackagePath)
                    && existingPackagePath.length() > closestParentPath.length()) {
                closestParentPath = existingPackagePath;
                closestParentName = packageElement.getName();
            }
        }

        if (!closestParentPath.isEmpty()) {
            Parent parent = new Parent(closestParentName, closestParentPath, Type.PACKAGE);
            PackageElement newPackage = new PackageElement(packageName, packagePath, parent);
            elementManager.addPackage(newPackage);
        } else {
            PackageElement newPackage = new PackageElement(packageName, packagePath);
            elementManager.addPackage(newPackage);
        }

        updatePackageParents(packageName, packagePath);
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

    private void updatePackageParents(String packageName, String packagePath) {
        List<PackageElement> packageElements = elementManager.getPackages();
        for (PackageElement packageElement : packageElements) {
            String otherPath = packageElement.getPath();
            if (otherPath.startsWith(packagePath)
                    && otherPath.length() > packagePath.length()) {
                Parent parent = new Parent(packageName, packagePath, Type.PACKAGE);
                packageElement.updateParent(parent);
                packageElement.updateShortName(otherPath.substring(packagePath.length(),
                        otherPath.length() - 1));
            }
        }
    }
}
