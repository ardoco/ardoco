package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CompilationUnitElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaModelMapper;
import generated.antlr.java.JavaLexer;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParser.CompilationUnitContext;


public class JavaExtractor extends ANTLRExtractor {
    private final ProgrammingLanguage language = ProgrammingLanguage.JAVA;
    private List<VariableElement> variables = new ArrayList<>();
    private List<ControlElement> controls = new ArrayList<>();
    private List<JavaClassElement> classes = new ArrayList<>();
    private List<InterfaceElement> interfaces = new ArrayList<>();
    private List<CompilationUnitElement> compilationUnits = new ArrayList<>();
    private List<PackageElement> packages = new ArrayList<>();


    public JavaExtractor(CodeItemRepository repository, String path) {
        super(repository, path);
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
        this.mapper = new JavaModelMapper(codeItemRepository, variables, controls, classes, interfaces, compilationUnits, packages, comments);
    }

    @Override
    protected CommentExtractor createCommentExtractor(CommonTokenStream tokens, String path) {
        return new JavaCommentExtractor(tokens, path);
    }

    public List<VariableElement> getVariables() {
        return variables;
    }

    public List<ControlElement> getControls() {
        return controls;
    }

    public List<JavaClassElement> getClasses() {
        return classes;
    }

    public List<InterfaceElement> getInterfaces() {
        return interfaces;
    }

    public List<CompilationUnitElement> getCompilationUnits() {
        return compilationUnits;
    }

    public List<PackageElement> getPackages() {
        return packages;
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
            extractControls(ctx);
            extractClasses(ctx);
            extractInterfaces(ctx);
            extractCompilationUnits(ctx);
            extractPackages();
            extractComments(file);
        }
    }

    private void extractVariables(CompilationUnitContext ctx) {
        JavaVariableExtractor variableExtractor = new JavaVariableExtractor();
        variables.addAll(variableExtractor.visitCompilationUnit(ctx));
    }

    private void extractControls(CompilationUnitContext ctx) {
        JavaControlExtractor controlExtractor = new JavaControlExtractor();
        controls.addAll(controlExtractor.visitCompilationUnit(ctx));
    }

    private void extractClasses(CompilationUnitContext ctx) {
        JavaClassExtractor classExtractor = new JavaClassExtractor();
        classes.addAll(classExtractor.visitCompilationUnit(ctx));
    }

    private void extractInterfaces(CompilationUnitContext ctx) {
        JavaInterfaceExtractor interfaceExtractor = new JavaInterfaceExtractor();
        interfaces.addAll(interfaceExtractor.visitCompilationUnit(ctx));
    }

    private void extractCompilationUnits(CompilationUnitContext ctx) {
        JavaCompilationUnitExtractor compilationUnitExtractor = new JavaCompilationUnitExtractor();
        compilationUnits.add(compilationUnitExtractor.visitCompilationUnit(ctx));
    }

    private void extractPackages() {
        for (CompilationUnitElement compilationUnit : compilationUnits) {
            PackageElement packageElement = compilationUnit.getPackage();
            if (!packages.contains(packageElement)) {
                packages.add(packageElement);
            }
        }
        packages.sort(Comparator.comparingInt(p -> p.getPackageNameParts(".").length));
    }
}
    

