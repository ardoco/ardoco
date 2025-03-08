package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaModelMapper;
import generated.antlr.java.JavaLexer;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParser.CompilationUnitContext;

public class JavaExtractor extends ANTLRExtractor {
    private final ProgrammingLanguage language = ProgrammingLanguage.JAVA;
    private final JavaElementManager elementManager;

    public JavaExtractor(CodeItemRepository repository, String path) {
        super(repository, path);
        this.elementManager = new JavaElementManager();
    }

    @Override
    protected List<Path> getFiles() {
        Path dir = Path.of(path);
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
    protected void extractFileContents(Path file) {
        try {
            CompilationUnitContext ctx = buildCompilationCtxForFile(file);
            extractElementsFromFile(ctx, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void mapToCodeModel() {
        if (!elementsExtracted) {
            throw new IllegalStateException("Elements have not been extracted yet.");
        }
        this.elementManager.addComments(comments);
        this.mapper = new JavaModelMapper(codeItemRepository, elementManager);
    }

    @Override
    protected CommentExtractor createCommentExtractor(CommonTokenStream tokens, String path) {
        return new JavaCommentExtractor(tokens, path);
    }

    public JavaElementManager getElementManager() {
        return elementManager;
    }

    private CompilationUnitContext buildCompilationCtxForFile(Path file) throws IOException {
        CharStream input = CharStreams.fromFileName(file.toString());
        JavaLexer lexer = new JavaLexer(input);
        this.tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        return parser.compilationUnit();
    }

    private void extractElementsFromFile(CompilationUnitContext ctx, Path file) {
        if (ctx != null) {
            extractVariables(ctx);
            extractFunctions(ctx);
            extractClasses(ctx);
            extractInterfaces(ctx);
            extractCompilationUnit(ctx, file);
            extractComments(file);
        }
    }

    private void extractVariables(CompilationUnitContext ctx) {
        JavaVariableExtractor variableExtractor = new JavaVariableExtractor();
        elementManager.addVariables(variableExtractor.visitCompilationUnit(ctx));
    }

    private void extractFunctions(CompilationUnitContext ctx) {
        JavaControlExtractor functionExtractor = new JavaControlExtractor();
        elementManager.addFunctions(functionExtractor.visitCompilationUnit(ctx));
    }

    private void extractClasses(CompilationUnitContext ctx) {
        JavaClassExtractor classExtractor = new JavaClassExtractor();
        elementManager.addClasses(classExtractor.visitCompilationUnit(ctx));
    }

    private void extractInterfaces(CompilationUnitContext ctx) {
        JavaInterfaceExtractor interfaceExtractor = new JavaInterfaceExtractor();
        elementManager.addInterfaces(interfaceExtractor.visitCompilationUnit(ctx));
    }

    private void extractCompilationUnit(CompilationUnitContext ctx, Path file) {
        JavaCompilationUnitExtractor compilationUnitExtractor = new JavaCompilationUnitExtractor();
        BasicElement compilationUnit = compilationUnitExtractor.visitCompilationUnit(ctx);
        if (compilationUnit.getParent() != null) {
            PackageElement packageElement = new PackageElement(compilationUnit.getParent().getName(), compilationUnit.getParent().getPath());
            elementManager.addPackage(packageElement);
        }
        elementManager.addCompilationUnit(compilationUnitExtractor.visitCompilationUnit(ctx));
    }
}
