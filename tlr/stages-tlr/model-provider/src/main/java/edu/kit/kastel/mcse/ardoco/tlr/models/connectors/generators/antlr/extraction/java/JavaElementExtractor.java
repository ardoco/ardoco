/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import edu.kit.kastel.mcse.ardoco.tlr.models.antlr4.java.JavaLexer;
import edu.kit.kastel.mcse.ardoco.tlr.models.antlr4.java.JavaParser;
import edu.kit.kastel.mcse.ardoco.tlr.models.antlr4.java.JavaParser.CompilationUnitContext;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ElementExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementStorageRegistry;

/**
 * Responsible for extracting structural elements from Java files. The extracted
 * elements are then stored in a JavaElementStorageRegistry.
 * The extraction process is done by building a token stream from a file via
 * ANTLR and extracting the elements from the token stream.
 * The files are identified by their file extension in the directory.
 */
public class JavaElementExtractor extends ElementExtractor {
    private final JavaElementStorageRegistry elementRegistry;

    public JavaElementExtractor() {
        super();
        this.elementRegistry = new JavaElementStorageRegistry();
        this.commentExtractor = new JavaCommentExtractor(elementRegistry);
    }

    public JavaElementExtractor(JavaElementStorageRegistry elementRegistry) {
        super();
        this.elementRegistry = elementRegistry;
        this.commentExtractor = new JavaCommentExtractor(elementRegistry);
    }

    @Override
    public JavaElementStorageRegistry getElements() {
        return new JavaElementStorageRegistry(elementRegistry);
    }

    @Override
    protected List<Path> getFiles(String directoryPath) {
        Path dir = Path.of(directoryPath);
        List<Path> javaFiles = new ArrayList<>();
        try (var files = Files.walk(dir)) {
            javaFiles.addAll(files.filter(Files::isRegularFile).filter(f -> f.toString().endsWith(".java")).toList());
        } catch (IOException e) {
            logger.error("I/O operation failed", e);
        }
        return javaFiles;
    }

    @Override
    protected CommonTokenStream buildTokens(Path absoluteFile, Path relativeFile) throws IOException {
        CharStream stream = CharStreams.fromReader(Files.newBufferedReader(absoluteFile), relativeFile.toString());
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

    public void visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
        List<String> imports = new ArrayList<>();
        for (JavaParser.ImportDeclarationContext importCtx : ctx.importDeclaration()) {
            if (importCtx.qualifiedName() != null) {
                String name = importCtx.qualifiedName().getText();
                if (importCtx.MUL() != null) {
                    name = name + ".*";
                }
                imports.add(name);
            }
        }
        ElementIdentifier identifier = addCompilationUnit(ctx, imports);
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
    }

    public ElementIdentifier visitClassDeclaration(JavaParser.ClassDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.identifier() == null) {
            return null;
        }
        String name = ctx.identifier().getText();
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
        addClass(identifier, parentIdentifier, extendsClass, implementedInterfaces, startLine, endLine);
        return identifier;
    }

    public ElementIdentifier visitEnumDeclaration(JavaParser.EnumDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.identifier() == null) {
            return null;
        }
        String name = ctx.identifier().getText();
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.CLASS);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addClass(identifier, parentIdentifier, startLine, endLine);
        return identifier;
    }

    public ElementIdentifier visitRecordDeclaration(JavaParser.RecordDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.identifier() == null) {
            return null;
        }
        String name = ctx.identifier().getText();
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.CLASS);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        // Records can only implement Interfaces, not extend classes
        List<String> implementedInterfaces = extractImplementedInterfaces(ctx);
        addClass(identifier, parentIdentifier, "", implementedInterfaces, startLine, endLine);
        return identifier;
    }

    public ElementIdentifier visitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.identifier() == null) {
            return null;
        }
        String name = ctx.identifier().getText();
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.INTERFACE);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addInterface(name, path, parentIdentifier, startLine, endLine);
        return identifier;
    }

    public ElementIdentifier visitMethodDeclaration(JavaParser.MethodDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.identifier() == null) {
            return null;
        }
        String name = ctx.identifier().getText();
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.FUNCTION);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        List<String> calleeNames = new ArrayList<>();
        if (ctx.methodBody() != null && ctx.methodBody().block() != null && ctx.methodBody().block().blockStatement() != null) {
            for (JavaParser.BlockStatementContext blockStatementContext : ctx.methodBody().block().blockStatement()) {
                if (blockStatementContext.localVariableDeclaration() != null) {
                    visitLocalVariableDeclaration(blockStatementContext.localVariableDeclaration(), identifier);
                }
            }
            collectCallNamesFromTree(ctx.methodBody().block(), calleeNames);
        }

        addFunction(name, path, parentIdentifier, startLine, endLine, calleeNames);
        return identifier;
    }

    public void visitFieldDeclaration(JavaParser.FieldDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.variableDeclarators() == null || ctx.typeType() == null) {
            return;
        }
        String variableType = ctx.typeType().getText();
        List<String> varNames = extractVariableNames(ctx.variableDeclarators().variableDeclarator());
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();
        addVariables(varNames, path, variableType, parentIdentifier, startLine, endLine);
    }

    public void visitLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.variableDeclarators() == null || ctx.typeType() == null) {
            return;
        }
        String variableType = ctx.typeType().getText();
        List<String> varNames = extractVariableNames(ctx.variableDeclarators().variableDeclarator());
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        addVariables(varNames, path, variableType, parentIdentifier, startLine, endLine);
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

    private void addVariables(List<String> varNames, String path, String variableType, ElementIdentifier parentIdentifier, int startLine, int endLine) {
        for (String variableName : varNames) {
            addVariable(variableName, path, variableType, parentIdentifier, startLine, endLine);
        }
    }

    private void addVariable(String variableName, String path, String variableType, ElementIdentifier parentIdentifier, int startLine, int endLine) {
        VariableElement variable = new VariableElement(variableName, path, variableType, parentIdentifier);
        variable.setStartLine(startLine);
        variable.setEndLine(endLine);
        elementRegistry.addVariable(variable);
    }

    private void addClass(ElementIdentifier identifier, ElementIdentifier parentIdentifier, String extendsClass, List<String> implementedInterfaces,
            int startLine, int endLine) {
        String name = identifier.name();
        String path = identifier.path();
        JavaClassElement classElement = new JavaClassElement(name, path, parentIdentifier, extendsClass, implementedInterfaces, startLine, endLine);
        elementRegistry.addClass(classElement);

    }

    private void addClass(ElementIdentifier identifier, ElementIdentifier parentIdentifier, int startLine, int endLine) {
        String name = identifier.name();
        String path = identifier.path();
        JavaClassElement classElement = new JavaClassElement(name, path, parentIdentifier, startLine, endLine);
        elementRegistry.addClass(classElement);
    }

    private void addFunction(String name, String path, ElementIdentifier parentIdentifier, int startLine, int endLine, List<String> calleeNames) {
        Type type = Type.FUNCTION;
        Element method = new Element(name, path, type, parentIdentifier, startLine, endLine);
        for (String callee : calleeNames) {
            method.addCalleeName(callee);
        }
        elementRegistry.addFunction(method);
    }

    private void collectCallNamesFromTree(ParseTree tree, List<String> names) {
        if (tree instanceof JavaParser.MethodCallExpressionContext mc && mc.methodCall().identifier() != null) {
            names.add(mc.methodCall().identifier().getText());
        } else if (tree instanceof JavaParser.MemberReferenceExpressionContext mr && mr.methodCall() != null && mr.methodCall().identifier() != null) {
            names.add(mr.methodCall().identifier().getText());
        } else if (tree instanceof JavaParser.ObjectCreationExpressionContext oc && oc.creator() != null && oc.creator().createdName() != null && !oc.creator()
                .createdName()
                .identifier()
                .isEmpty()) {
            List<JavaParser.IdentifierContext> ids = oc.creator().createdName().identifier();
            names.add(ids.get(ids.size() - 1).getText());
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            collectCallNamesFromTree(tree.getChild(i), names);
        }
    }

    private void addInterface(String name, String path, ElementIdentifier parentIdentifier, int startLine, int endLine) {
        Type type = Type.INTERFACE;
        Element interfaceElement = new Element(name, path, type, parentIdentifier, startLine, endLine);
        elementRegistry.addInterface(interfaceElement);
    }

    private ElementIdentifier addCompilationUnit(JavaParser.CompilationUnitContext ctx, List<String> imports) {
        Type type = Type.COMPILATIONUNIT;
        Element compilationUnit = null;
        String path = PathExtractor.extractPath(ctx);
        String name = PathExtractor.extractNameFromPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, type);
        if (ctx.packageDeclaration() != null && ctx.packageDeclaration().qualifiedName() != null) {
            String packagePath = path.substring(0, path.lastIndexOf("/") + 1);
            String packageName = resolveNameFromPath(packagePath);
            addPackage(packageName, packagePath);
            ElementIdentifier packageIdentifier = new ElementIdentifier(packageName, packagePath, Type.PACKAGE);
            compilationUnit = new Element(name, path, type, packageIdentifier);
        } else {
            compilationUnit = new Element(name, path, type);
        }
        for (String imp : imports) {
            compilationUnit.addImport(imp);
        }
        elementRegistry.addCompilationUnit(compilationUnit);
        return identifier;
    }

    private void addPackage(String packageName, String packagePath) {
        for (PackageElement packageElement : elementRegistry.getPackages()) {
            if (packageElement.getPath().equals(packagePath)) {
                return;
            }
        }

        String parentPath = resolveParentPath(packagePath);
        if (!parentPath.isEmpty()) {
            String parentName = resolveNameFromPath(parentPath);
            addPackage(parentName, parentPath);
            ElementIdentifier parentIdentifier = new ElementIdentifier(parentName, parentPath, Type.PACKAGE);
            elementRegistry.addPackage(new PackageElement(packageName, packagePath, parentIdentifier));
        } else {
            elementRegistry.addPackage(new PackageElement(packageName, packagePath));
        }
    }

    private String resolveParentPath(String packagePath) {
        if (packagePath.isEmpty()) {
            return "";
        }
        String withoutTrailingSlash = packagePath.substring(0, packagePath.length() - 1);
        int lastSlash = withoutTrailingSlash.lastIndexOf('/');
        return lastSlash < 0 ? "" : withoutTrailingSlash.substring(0, lastSlash + 1);
    }

    private String resolveNameFromPath(String path) {
        String withoutTrailingSlash = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlash = withoutTrailingSlash.lastIndexOf('/');
        return lastSlash < 0 ? withoutTrailingSlash : withoutTrailingSlash.substring(lastSlash + 1);
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
            for (JavaParser.TypeTypeContext typeTypeContext : ctx.typeType()) {// Extract the text and remove generic type parameters
                String typeName = typeTypeContext.getText();
                String simpleName = typeName.replaceAll("<.*?>", ""); // Remove everything inside <>
                implementedInterfaces.add(simpleName);
            }
        }
        return implementedInterfaces;
    }

}
