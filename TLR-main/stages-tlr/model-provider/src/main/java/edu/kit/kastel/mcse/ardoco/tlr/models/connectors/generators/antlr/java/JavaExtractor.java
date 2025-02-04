package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CompilationUnitElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mappers.JavaModelMapper;
import generated.antlr.JavaLexer;
import generated.antlr.JavaParser;
import generated.antlr.JavaParser.CompilationUnitContext;
public class JavaExtractor extends ANTLRExtractor {
    private List<VariableElement> variables = new ArrayList<>();
    private List<ControlElement> controls = new ArrayList<>();
    private List<ClassElement> classes = new ArrayList<>();
    private List<InterfaceElement> interfaces = new ArrayList<>();
    private List<CompilationUnitElement> compilationUnits = new ArrayList<>();
    private List<PackageElement> packages = new ArrayList<>();
    private CompilationUnitContext tree;
    private JavaModelMapper mapper;
    private boolean extracted;


    public JavaExtractor(CodeItemRepository repository, String path) {
        super(repository, path);
        this.extracted = false;
    }

    public void execute() throws IOException {
        List<Path> files = getJavaFiles();
        for (Path file : files) {
            buildCompilationCtxForFile(file);
            extractElementsFromFile(file);
        }
    }

    public void buildMapper() {
        if (!extracted) {
            throw new IllegalStateException("Elements have not been extracted yet.");
        }
        this.mapper = new JavaModelMapper(this.codeItemRepository, variables, controls, classes, interfaces, compilationUnits, packages);
    }

    public JavaModelMapper getMapper() {
        return mapper;
    }

    public List<VariableElement> getVariables() {
        return variables;
    }

    public List<ControlElement> getControls() {
        return controls;
    }

    public List<ClassElement> getClasses() {
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

    private List<Path> getJavaFiles() throws IOException {
        Path dir = Path.of(this.path);
        List<Path> javaFiles = new ArrayList<>();
        Files.walk(dir)
             .filter(Files::isRegularFile)
             .filter(path -> path.toString().endsWith(".java")) // Filter for .java files
             .forEach(javaFiles::add);
        return javaFiles;
    }

    private void buildCompilationCtxForFile(Path file) throws IOException {
        CharStream input = CharStreams.fromFileName(file.toString());
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        this.tree = parser.compilationUnit();
    }

    private void extractElementsFromFile(Path file) {
        if (this.tree != null) {
            extractVariables();
            extractControls();
            extractClasses();
            extractInterfaces();
            extractCompilationUnits(file);
            extractPackages();
        }
        this.extracted = true;
    }

    private void extractVariables() {
        JavaVariableExtractor variableExtractor = new JavaVariableExtractor();
        variables.addAll(variableExtractor.visitCompilationUnit(tree));
    }

    private void extractControls() {
        JavaControlExtractor controlExtractor = new JavaControlExtractor();
        controls.addAll(controlExtractor.visitCompilationUnit(tree));
    }

    private void extractClasses() {
        JavaClassExtractor classExtractor = new JavaClassExtractor();
        classes.addAll(classExtractor.visitCompilationUnit(tree));
    }

    private void extractInterfaces() {
        JavaInterfaceExtractor interfaceExtractor = new JavaInterfaceExtractor();
        interfaces.addAll(interfaceExtractor.visitCompilationUnit(tree));
    }

    private void extractCompilationUnits(Path file) {
        JavaCompilationUnitExtractor compilationUnitExtractor = new JavaCompilationUnitExtractor(file.toString());
        compilationUnits.add(compilationUnitExtractor.visitCompilationUnit(tree));
    }

    private void extractPackages() {
        for (CompilationUnitElement compilationUnit : compilationUnits) {
            PackageElement packageElement = compilationUnit.getPackage();
            if (!packages.contains(packageElement)) {
                packages.add(packageElement);
            }
        }
        packages.sort(Comparator.comparingInt(p -> p.getPackageNameParts().length));
    }

}
    

