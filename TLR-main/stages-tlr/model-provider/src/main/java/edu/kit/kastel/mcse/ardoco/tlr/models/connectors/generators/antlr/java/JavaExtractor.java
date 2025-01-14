package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.java;


import java.io.IOError;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mappers.JavaModelMapper;
import generated.antlr.JavaLexer;
import generated.antlr.JavaParser;
import generated.antlr.JavaParser.CompilationUnitContext;
public class JavaExtractor extends ANTLRExtractor {
    private ProgrammingLanguage language = ProgrammingLanguage.JAVA;
    private List<VariableElement> variables = new ArrayList<>();
    private List<ControlElement> controls = new ArrayList<>();
    private List<ClassElement> classes = new ArrayList<>();
    private List<InterfaceElement> interfaces = new ArrayList<>();
    private CompilationUnitContext tree;
    private JavaModelMapper mapper = new JavaModelMapper();

    public JavaExtractor(CodeItemRepository repository, String path) {
        super(repository, path);
    }

    public void buildParseTree(Path file, Path dir) throws IOException {
        CharStream input = CharStreams.fromFileName(file.toString());
        
        JavaLexer lexer = new JavaLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        
        this.tree = parser.compilationUnit();
    }

    public void extractAllFromParseTree() {
        if (this.tree != null) {
            extractVariables();
            extractControls();
            extractClasses();
            extractInterfaces();
            extractCompilationUnits();
        }
    }

    public void mapToCodeModel() {
        //mapper.mapVariables(variables);
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

    private void extractCompilationUnits() {
        // JavaCompilationUnitExtractor compilationUnitExtractor = new JavaCompilationUnitExtractor();
        // compilationUnitExtractor.visit(tree);
    }







}
    

