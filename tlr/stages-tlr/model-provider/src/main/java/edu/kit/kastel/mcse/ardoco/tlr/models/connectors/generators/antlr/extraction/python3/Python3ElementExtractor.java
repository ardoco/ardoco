/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import edu.kit.kastel.mcse.ardoco.tlr.models.antlr4.python3.PythonLexer;
import edu.kit.kastel.mcse.ardoco.tlr.models.antlr4.python3.PythonParser;
import edu.kit.kastel.mcse.ardoco.tlr.models.antlr4.python3.PythonParser.File_inputContext;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ElementExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.PathExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3.Python3ElementStorageRegistry;

/**
 * Responsible for extracting structural elements from Python3 files. The
 * extracted elements are stored in a Python3ElementStorageRegistry.
 * The extraction process is done by building a token stream from a file via
 * ANTLR and extracting the elements from the token stream.
 * The files are identified by their suffix in the directory.
 */
@SuppressWarnings("java:S100")
public class Python3ElementExtractor extends ElementExtractor {
    private final Python3ElementStorageRegistry elementRegistry;
    private final LinkedHashMap<String, List<String>> pendingImportsByPath = new LinkedHashMap<>();

    public Python3ElementExtractor() {
        super();
        this.elementRegistry = new Python3ElementStorageRegistry();
        this.commentExtractor = new Python3CommentExtractor(elementRegistry);
    }

    public Python3ElementExtractor(Python3ElementStorageRegistry elementRegistry) {
        super();
        this.elementRegistry = elementRegistry;
        this.commentExtractor = new Python3CommentExtractor(elementRegistry);
    }

    @Override
    public Python3ElementStorageRegistry getElements() {
        return new Python3ElementStorageRegistry(elementRegistry);
    }

    @Override
    protected List<Path> getFiles(String directoryPath) {
        Path dir = Path.of(directoryPath);
        List<Path> pythonFiles = new ArrayList<>();
        try (var files = Files.walk(dir)) {
            pythonFiles.addAll(files.filter(Files::isRegularFile).filter(f -> f.toString().endsWith(".py")).toList());
        } catch (IOException e) {
            logger.error("I/O operation failed", e);
        }
        return pythonFiles;
    }

    @Override
    protected CommonTokenStream buildTokens(Path absoluteFile, Path relativeFile) throws IOException {
        CharStream charStream = CharStreams.fromReader(Files.newBufferedReader(absoluteFile), relativeFile.toString());
        PythonLexer lexer = new PythonLexer(charStream);
        return new CommonTokenStream(lexer);
    }

    @Override
    public void extractElements(CommonTokenStream tokens) {
        File_inputContext ctx = buildContext(tokens);
        visitFile_input(ctx);
        addModules(ctx);
    }

    private File_inputContext buildContext(CommonTokenStream tokenStream) {
        PythonParser parser = new PythonParser(tokenStream);
        return parser.file_input();
    }

    public void visitFile_input(PythonParser.File_inputContext ctx) {
        ElementIdentifier parentIdentifier = new ElementIdentifier(PathExtractor.extractNameFromPath(ctx), PathExtractor.extractPath(ctx), Type.MODULE);
        if (ctx.statements() != null) {
            for (PythonParser.StatementContext stmt : ctx.statements().statement()) {
                visitStatement(stmt, parentIdentifier);
            }
        }
    }

    public void visitStatement(PythonParser.StatementContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.simple_stmts() != null) {
            visitSimple_stmts(ctx.simple_stmts(), parentIdentifier);
        } else if (ctx.compound_stmt() != null) {
            visitCompound_stmt(ctx.compound_stmt(), parentIdentifier);
        }
    }

    public void visitSimple_stmts(PythonParser.Simple_stmtsContext ctx, ElementIdentifier parentIdentifier) {
        for (PythonParser.Simple_stmtContext simpleStmt : ctx.simple_stmt()) {
            visitSimple_stmt(simpleStmt, parentIdentifier);
        }
    }

    public void visitCompound_stmt(PythonParser.Compound_stmtContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.function_def() != null) {
            visitFunction_def(ctx.function_def(), parentIdentifier);
        } else if (ctx.class_def() != null) {
            visitClass_def(ctx.class_def(), parentIdentifier);
        }
    }

    public ElementIdentifier visitClass_def(PythonParser.Class_defContext ctx, ElementIdentifier parentIdentifier) {
        PythonParser.Class_def_rawContext raw = ctx.class_def_raw();
        if (raw == null || raw.name() == null) {
            return null;
        }
        String name = raw.name().getText();
        String path = PathExtractor.extractPath(ctx);
        List<String> childClassOf = getParentClasses(raw);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.CLASS);
        int startLine = raw.getStart().getLine();
        int endLine = lastContentLine(raw);

        visitBlockStatements(raw.block(), identifier);

        addClassElement(name, path, parentIdentifier, childClassOf, startLine, endLine);
        return identifier;
    }

    public ElementIdentifier visitFunction_def(PythonParser.Function_defContext ctx, ElementIdentifier parentIdentifier) {
        PythonParser.Function_def_rawContext raw = ctx.function_def_raw();
        if (raw == null || raw.name() == null) {
            return null;
        }
        String name = raw.name().getText();
        String path = PathExtractor.extractPath(ctx);
        ElementIdentifier identifier = new ElementIdentifier(name, path, Type.FUNCTION);
        int startLine = raw.getStart().getLine();
        int endLine = lastContentLine(raw);

        List<String> calleeNames = new ArrayList<>();
        PythonParser.BlockContext block = raw.block();
        if (block != null) {
            visitBlockStatements(block, identifier);
            collectCallNamesFromTree(block, calleeNames);
        }
        addFunctionElement(name, path, parentIdentifier, startLine, endLine, calleeNames);
        return identifier;
    }

    private void visitBlockStatements(PythonParser.BlockContext block, ElementIdentifier parentIdentifier) {
        if (block == null) {
            return;
        }
        if (block.statements() != null) {
            for (PythonParser.StatementContext stmt : block.statements().statement()) {
                visitStatement(stmt, parentIdentifier);
            }
        } else if (block.simple_stmts() != null) {
            visitSimple_stmts(block.simple_stmts(), parentIdentifier);
        }
    }

    public void visitSimple_stmt(PythonParser.Simple_stmtContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.assignment() != null) {
            visitAssignment(ctx.assignment(), parentIdentifier);
        } else if (ctx.import_stmt() != null) {
            visitImport_stmt(ctx.import_stmt());
        }
    }

    private void visitImport_name(PythonParser.Import_nameContext ctx, List<String> importedNames) {
        for (PythonParser.Dotted_as_nameContext dotted : ctx.dotted_as_names().dotted_as_name()) {
            importedNames.add(dotted.dotted_name().getText());
        }
    }

    private void visitImport_from(PythonParser.Import_fromContext ctx, List<String> importedNames) {
        String base = ctx.dotted_name() != null ? ctx.dotted_name().getText() : "";
        PythonParser.Import_from_targetsContext targets = ctx.import_from_targets();
        if (targets != null && targets.import_from_as_names() != null) {
            for (PythonParser.Import_from_as_nameContext name : targets.import_from_as_names().import_from_as_name()) {
                importedNames.add(base.isEmpty() ? name.name(0).getText() : base + "." + name.name(0).getText());
            }
        } else if (!base.isEmpty()) {
            importedNames.add(base);
        }
    }

    public void visitImport_stmt(PythonParser.Import_stmtContext ctx) {
        List<String> importedNames = new ArrayList<>();
        if (ctx.import_name() != null) {
            visitImport_name(ctx.import_name(), importedNames);
        } else if (ctx.import_from() != null) {
            visitImport_from(ctx.import_from(), importedNames);
        }
        String importPath = PathExtractor.extractPath(ctx);
        if (!pendingImportsByPath.containsKey(importPath)) {
            pendingImportsByPath.put(importPath, new ArrayList<>());
        }
        pendingImportsByPath.get(importPath).addAll(importedNames);
    }

    public void visitAssignment(PythonParser.AssignmentContext ctx, ElementIdentifier parentIdentifier) {
        if (!ctx.star_targets().isEmpty() && ctx.annotated_rhs() != null) {
            extractVariablesFromAssignment(ctx, parentIdentifier);
        }
    }

    private List<String> getParentClasses(PythonParser.Class_def_rawContext ctx) {
        List<String> parentClasses = new ArrayList<>();
        if (ctx.arguments() != null && ctx.arguments().args() != null) {
            for (PythonParser.ExpressionContext arg : ctx.arguments().args().expression()) {
                parentClasses.add(arg.getText());
            }
        }
        return parentClasses;
    }

    private void extractVariablesFromAssignment(PythonParser.AssignmentContext ctx, ElementIdentifier parentIdentifier) {
        if (ctx.star_targets().size() != 1) {
            return;
        }
        List<String> varNames = extractTargetNames(ctx.star_targets(0));
        List<String> values = extractRhsValues(ctx.annotated_rhs());
        List<String> types = inferTypesFromValues(values);
        String path = PathExtractor.extractPath(ctx);
        int startLine = ctx.getStart().getLine();
        int endLine = ctx.getStop().getLine();

        if (varNames.size() != values.size()) {
            return;
        }

        for (int i = 0; i < varNames.size(); i++) {
            addVariableElement(varNames.get(i), path, types.get(i), parentIdentifier, startLine, endLine);
        }
    }

    private List<String> extractTargetNames(PythonParser.Star_targetsContext targets) {
        List<String> variableNames = new ArrayList<>();
        for (PythonParser.Star_targetContext target : targets.star_target()) {
            variableNames.add(target.getText());
        }
        return variableNames;
    }

    private List<String> extractRhsValues(PythonParser.Annotated_rhsContext rhs) {
        List<String> values = new ArrayList<>();
        if (rhs.star_expressions() != null) {
            for (PythonParser.Star_expressionContext value : rhs.star_expressions().star_expression()) {
                values.add(value.getText());
            }
        }
        return values;
    }

    private List<String> inferTypesFromValues(List<String> values) {
        List<String> types = new ArrayList<>();

        for (String value : values) {
            types.add(inferTypeFromValue(value));
        }
        return types;
    }

    private String inferTypeFromValue(String value) {
        if (value.matches("^-?\\d+$")) {
            return "int";
        } else if (value.matches("^-?\\d+\\.\\d+$")) {
            return "float";
        } else if (value.matches("^\".*\"$")) {
            return "str";
        } else if (value.equals("True") || value.equals("False")) {
            return "bool";
        } else {
            /*
             * Later need to check if it is a Class Object cannot be done here
             * as it requires all classes to be parsed already
             */
            return "any";
        }
    }

    private void addVariableElement(String varName, String path, String type, ElementIdentifier parentIdentifier, int startLine, int endLine) {
        VariableElement variable = new VariableElement(varName, path, type, parentIdentifier, startLine, endLine);
        elementRegistry.addVariable(variable);
    }

    private void addFunctionElement(String name, String path, ElementIdentifier parentIdentifier, int startLine, int endLine, List<String> calleeNames) {
        Type type = Type.FUNCTION;
        Element function = new Element(name, path, type, parentIdentifier, startLine, endLine);
        for (String callee : calleeNames) {
            function.addCalleeName(callee);
        }
        elementRegistry.addFunction(function);
    }

    private int lastContentLine(ParseTree tree) {
        for (int i = tree.getChildCount() - 1; i >= 0; i--) {
            ParseTree child = tree.getChild(i);
            if (child instanceof TerminalNode terminal) {
                if (isLayoutToken(terminal.getSymbol().getType())) {
                    continue;
                }
                return terminal.getSymbol().getLine();
            }
            int childLine = lastContentLine(child);
            if (childLine > 0) {
                return childLine;
            }
        }
        return 0;
    }

    private boolean isLayoutToken(int tokenType) {
        return tokenType == PythonParser.NEWLINE || tokenType == PythonParser.INDENT || tokenType == PythonParser.DEDENT || tokenType == PythonLexer.EOF;
    }

    private void collectCallNamesFromTree(ParseTree tree, List<String> names) {
        if (tree instanceof PythonParser.PrimaryContext primary) {
            collectCallNameFromPrimary(primary, names);
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            collectCallNamesFromTree(tree.getChild(i), names);
        }
    }

    private void collectCallNameFromPrimary(PythonParser.PrimaryContext primary, List<String> names) {
        if (!isCallPrimary(primary)) {
            return;
        }
        PythonParser.PrimaryContext callee = primary.primary();
        if (callee == null) {
            return;
        }
        if (callee.name() != null) {
            names.add(callee.name().getText());
        } else if (callee.atom() != null && callee.atom().name() != null) {
            names.add(callee.atom().name().getText());
        }
    }

    private boolean isCallPrimary(PythonParser.PrimaryContext primary) {
        if (primary.primary() == null) {
            return false;
        }
        for (int i = 0; i < primary.getChildCount(); i++) {
            ParseTree child = primary.getChild(i);
            if (child instanceof TerminalNode terminal && "(".equals(terminal.getText())) {
                return true;
            }
        }
        return false;
    }

    private void addClassElement(String name, String path, ElementIdentifier parentIdentifier, List<String> childClassOf, int startLine, int endLine) {
        ClassElement python3ClassElement = new ClassElement(name, path, parentIdentifier, startLine, endLine, childClassOf);
        elementRegistry.addClass(python3ClassElement);
    }

    private void addModules(PythonParser.File_inputContext ctx) {
        Type type = Type.MODULE;
        String name = PathExtractor.extractNameFromPath(ctx);
        String path = PathExtractor.extractPath(ctx);
        String packagePath = path.substring(0, path.lastIndexOf("/") + 1);
        String packageName = addPackage(packagePath);
        ElementIdentifier parentIdentifier = new ElementIdentifier(packageName, packagePath, Type.PACKAGE);
        Element module = new Element(name, path, type, parentIdentifier);
        List<String> imports = pendingImportsByPath.containsKey(path) ? pendingImportsByPath.get(path) : List.of();
        for (String imp : imports) {
            module.addImport(imp);
        }
        elementRegistry.addModule(module);
    }

    private String addPackage(String packagePath) {
        for (PackageElement packageElement : elementRegistry.getPackages()) {
            if (packageElement.getPath().equals(packagePath)) {
                return packageElement.getName();
            }
        }

        String parentPath = resolveParentPath(packagePath);
        if (!parentPath.isEmpty()) {
            String parentName = addPackage(parentPath);
            String packageName = packagePath.substring(parentPath.length(), packagePath.length() - 1);
            ElementIdentifier parentIdentifier = new ElementIdentifier(parentName, parentPath, Type.PACKAGE);
            elementRegistry.addPackage(new PackageElement(packageName, packagePath, parentIdentifier));
            return packageName;
        } else {
            String packageName = packagePath.isEmpty() ? "" : packagePath.substring(0, packagePath.length() - 1);
            elementRegistry.addPackage(new PackageElement(packageName, packagePath));
            return packageName;
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

}
