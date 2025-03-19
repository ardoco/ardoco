package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ElementExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementStorageRegistry;
import generated.antlr.java.JavaLexer;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParser.CompilationUnitContext;

public class JavaElementExtractor extends ElementExtractor {
    private final JavaElementStorageRegistry elementManager;

    public JavaElementExtractor() {
        super();
        this.elementManager = new JavaElementStorageRegistry();
        this.commentExtractor = new JavaCommentExtractor(elementManager);
    }

    public JavaElementExtractor(JavaElementStorageRegistry elementManager) {
        super();
        this.elementManager = elementManager;
        this.commentExtractor = new JavaCommentExtractor(elementManager);
    }

    @Override
    public JavaElementStorageRegistry getElements() {
        return elementManager;
    }

        @Override
    protected List<Path> getFiles(String directoryPath) {
        Path dir = Path.of(directoryPath);
        List<Path> javaFiles = new ArrayList<>();
        try {
            Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".java"))
                    .forEach(javaFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return javaFiles;
    }

    @Override
    protected CommonTokenStream buildTokens(Path file) throws IOException {
        CharStream stream = CharStreams.fromPath(file);
        JavaLexer lexer = new JavaLexer(stream);
        return new CommonTokenStream(lexer);
    }

    @Override
    public void extractElements(CommonTokenStream tokens) {
        CompilationUnitContext ctx = buildContext(tokens);
        visitCompilationUnit(ctx);

    }

    private CompilationUnitContext buildContext(CommonTokenStream tokenStream) {
        JavaParser parser = new JavaParser(tokenStream);
        return parser.compilationUnit();
    }

    public Void visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        ElementIdentifier identifier = addCompilationUnit(ctx);
        for (JavaParser.TypeDeclarationContext typeDeclarationContext : ctx.typeDeclaration()) {
            if (typeDeclarationContext.classDeclaration() != null) {
                visitClassDeclaration(typeDeclarationContext.classDeclaration(), identifier);
            } else if (typeDeclarationContext.enumDeclaration() != null) {
                visitEnumDeclaration(typeDeclarationContext.enumDeclaration(), identifier);
            } else if (typeDeclarationContext.recordDeclaration() != null) {
                visitRecordDeclaration(typeDeclarationContext.recordDeclaration(), identifier);
            } else if (typeDeclarationContext.interfaceDeclaration() != null) {
                visitInterfaceDeclaration(typeDeclarationContext.interfaceDeclaration(), identifier);
            }
        }
        return null;
    }

    public ElementIdentifier visitClassDeclaration(JavaParser.ClassDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.identifier() == null) {
            return null;
        }
        String name = ctx.identifier().getText();
        ElementIdentifier parent = parentIdentifier;
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.CLASS);
        String extendsClass = getExtendsClass(ctx);
        List<String> implementedInterfaces = extractImplementedInterfaces(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        if (ctx.classBody() != null && ctx.classBody().classBodyDeclaration() != null) {
            for (JavaParser.ClassBodyDeclarationContext classBodyDeclarationContext : ctx.classBody().classBodyDeclaration()) {
                if (classBodyDeclarationContext.memberDeclaration() != null) {
                    if (classBodyDeclarationContext.memberDeclaration().methodDeclaration() != null) {
                        visitMethodDeclaration(classBodyDeclarationContext.memberDeclaration().methodDeclaration(), identifier);
                    } else if (classBodyDeclarationContext.memberDeclaration().fieldDeclaration() != null) {
                        visitFieldDeclaration(classBodyDeclarationContext.memberDeclaration().fieldDeclaration(), identifier);
                    } else if (classBodyDeclarationContext.memberDeclaration().classDeclaration() != null) {
                        visitClassDeclaration(classBodyDeclarationContext.memberDeclaration().classDeclaration(), identifier);
                    } else if (classBodyDeclarationContext.memberDeclaration().interfaceDeclaration() != null) {
                        visitInterfaceDeclaration(classBodyDeclarationContext.memberDeclaration().interfaceDeclaration(), identifier);
                    }
                }
            }
        }
        addClass(identifier, parent, extendsClass, implementedInterfaces, startLine, endLine);
        return identifier;
    }

    public ElementIdentifier visitEnumDeclaration(JavaParser.EnumDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.identifier() == null) {
            return null;
        }
        String name = ctx.identifier().getText();
        ElementIdentifier parent = parentIdentifier;
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.CLASS);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addClass(identifier, parent, startLine, endLine);
        return identifier;
    }

    public ElementIdentifier visitRecordDeclaration(JavaParser.RecordDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.identifier() == null) {
            return null;
        }
        String name = ctx.identifier().getText();
        ElementIdentifier parent = parentIdentifier;
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.CLASS);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        // Records can only implement Interfaces, not extend classes
        List<String> implementedInterfaces = extractImplementedInterfaces(ctx);
        addClass(identifier, parent, "", implementedInterfaces, startLine, endLine);
        return identifier;
    }

    public ElementIdentifier visitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.identifier() == null) {
            return null;
        }
        String name = ctx.identifier().getText();
        ElementIdentifier parent = parentIdentifier;
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.INTERFACE);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addInterface(name, path, parent, startLine, endLine);
        return identifier;
    }


    public ElementIdentifier visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx,
            ElementIdentifier parentIdentifier) {
        if (ctx.identifier() == null) {
            return null;
        }
        String name = ctx.identifier().getText();
        ElementIdentifier parent = parentIdentifier;
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.FUNCTION);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        if (ctx.methodBody() != null && ctx.methodBody().block() != null
                && ctx.methodBody().block().blockStatement() != null) {
            for (JavaParser.BlockStatementContext blockStatementContext : ctx.methodBody().block().blockStatement()) {
                if (blockStatementContext.localVariableDeclaration() != null) {
                    visitLocalVariableDeclaration(blockStatementContext.localVariableDeclaration(), identifier);
                }
            }

        }

        addFunction(name, path, parent, startLine, endLine);
        return identifier;
    }

    public Void visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.variableDeclarators() == null || ctx.typeType() == null) {
            return null;
        }
        String variableType = ctx.typeType().getText();
        ElementIdentifier parent = parentIdentifier;
        List<String> varNames = extractVariableNames(ctx.variableDeclarators().variableDeclarator());
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();
        addVariables(varNames, path, variableType, parent, startLine, endLine);
        return null;
    }

    public Void visitLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.variableDeclarators() == null || ctx.typeType() == null) {
            return null;
        }
        String variableType = ctx.typeType().getText();
        ElementIdentifier parent = parentIdentifier;
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
            if (variableDeclarator.variableDeclaratorId() == null || variableDeclarator.variableDeclaratorId().identifier() == null) {
                continue;
            }
            String name = variableDeclarator.variableDeclaratorId().identifier().getText();
            variableNames.add(name);
        }
        return variableNames;
    }

    private void addVariables(List<String> varNames, String path, String variableType, ElementIdentifier parent,
            int startLine, int endLine) {
        for (String variableName : varNames) {
            addVariable(variableName, path, variableType, parent, startLine, endLine);
        }
    }

    private void addVariable(String variableName, String path, String variableType, ElementIdentifier parent,
            int startLine, int endLine) {
        VariableElement variable = new VariableElement(variableName, path, variableType, parent);
        variable.setStartLine(startLine);
        variable.setEndLine(endLine);
        elementManager.addVariable(variable);
    }

    private void addClass(ElementIdentifier identifier, ElementIdentifier parent, String extendsClass,
            List<String> implementedInterfaces,
            int startLine, int endLine) {
        String name = identifier.name();
        String path = identifier.path();
        JavaClassElement classElement = new JavaClassElement(name, path, parent, extendsClass,
                implementedInterfaces, startLine, endLine);
        elementManager.addClass(classElement);

    }

    private void addClass(ElementIdentifier identifier, ElementIdentifier parent, int startLine, int endLine) {
        String name = identifier.name();
        String path = identifier.path();
        JavaClassElement classElement = new JavaClassElement(name, path, parent, startLine, endLine);
        elementManager.addClass(classElement);
    }

    private void addFunction(String name, String path, ElementIdentifier parent, int startLine, int endLine) {
        Type type = Type.FUNCTION;
        Element method = new Element(name, path, type, parent, startLine, endLine);
        elementManager.addFunction(method);
    }

    private void addInterface(String name, String path, ElementIdentifier parent,
            int startLine, int endLine) {
        Type type = Type.INTERFACE;
        Element interfaceElement = new Element(name, path, type, parent, startLine, endLine);
        elementManager.addInterface(interfaceElement);
    }

    private ElementIdentifier addCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        Type type = Type.COMPILATIONUNIT;
        Element compilationUnit = null;
        String path = PathExtractor.extractPath(ctx);
        String name = PathExtractor.extractNameFromPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, type);
        if (ctx.packageDeclaration() != null && ctx.packageDeclaration().qualifiedName() != null) {
            String packageName = ctx.packageDeclaration().qualifiedName().getText();
            String packagePath = path.substring(0, path.lastIndexOf("/") + 1);
            ElementIdentifier packageIdentifier = new ElementIdentifier(packageName, packagePath, Type.PACKAGE);
            addPackage(packageIdentifier);
            ElementIdentifier parent = packageIdentifier;
            compilationUnit = new Element(name, path, type, parent);
        } else {
            compilationUnit = new Element(name, path, type);
        }
        elementManager.addCompilationUnit(compilationUnit);
        return identifier;
    }

    private void addPackage(ElementIdentifier packageIdentifier) {
        List<PackageElement> packageElements = elementManager.getPackages();
        String closestParentName = "";
        String closestParentPath = "";
        String packagePath = packageIdentifier.path();
        String packageName = packageIdentifier.name();
        for (PackageElement packageElement : packageElements) {
            String existingPackagePath = packageElement.getPath();
            if (packagePath.startsWith(existingPackagePath)
                    && existingPackagePath.length() > closestParentPath.length()) {
                closestParentPath = existingPackagePath;
                closestParentName = packageElement.getName();
            }
        }

        if (!closestParentPath.isEmpty()) {
            ElementIdentifier parent = new ElementIdentifier(closestParentName, closestParentPath, Type.PACKAGE);
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
                ElementIdentifier parent = new ElementIdentifier(packageName, packagePath, Type.PACKAGE);
                packageElement.updateParent(parent);
                packageElement.updateShortName(otherPath.substring(packagePath.length(),
                        otherPath.length() - 1));
            }
        }
    }
}
