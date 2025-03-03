package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;


import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ModuleElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.Python3ModelMapper;
import generated.antlr.python3.Python3Lexer;
import generated.antlr.python3.Python3Parser;
import generated.antlr.python3.Python3Parser.File_inputContext;

public class Python3Extractor extends ANTLRExtractor {
    private ProgrammingLanguage language = ProgrammingLanguage.PYTHON3;
    private List<Python3VariableElement> variables = new ArrayList<>();
    private List<ControlElement> controls = new ArrayList<>();
    private List<ClassElement> classes = new ArrayList<>();
    private List<Python3ModuleElement> modules = new ArrayList<>();
    private List<PackageElement> packages = new ArrayList<>();
    


    public Python3Extractor(CodeItemRepository repository, String path) {
        super(repository, path);
    }

    @Override
    protected List<Path> getFiles() {
        Path dir = Path.of(path);
        List<Path> pythonFiles = new ArrayList<>();
        try {
            Files.walk(dir)
            .filter(Files::isRegularFile)
            .filter(f -> f.toString().endsWith(".py"))
            .forEach(pythonFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pythonFiles;
    }

    @Override
    protected void extractFileContents(Path file) {
        try {
            File_inputContext ctx = buildFileInputContext(file);
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
        this.mapper = new Python3ModelMapper(this.codeItemRepository, variables, controls, classes, modules, packages, comments);
    }

    @Override
    protected CommentExtractor createCommentExtractor(CommonTokenStream tokens, String path) {
        return new Python3CommentExtractor(tokens, path);
    }

    public List<Python3VariableElement> getVariables() {
        return variables;
    }

    public List<ControlElement> getControls() {
        return controls;
    }

    public List<ClassElement> getClasses() {
        return classes;
    }

    public List<Python3ModuleElement> getModules() {
        return modules;
    }

    public List<PackageElement> getPackages() {
        return packages;
    }

    private File_inputContext buildFileInputContext(Path file) throws IOException {
        Python3Lexer lexer = new Python3Lexer(CharStreams.fromPath(file));
        super.tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        return parser.file_input();
    }

    private void extractElementsFromFile(File_inputContext ctx, Path file) {
        if (ctx != null) {
            extractVariables(ctx);
            extractControls(ctx);
            extractClasses(ctx);
            extractModules(ctx);
            extractPackages();
            extractComments(file);
        }
    }

    private void extractVariables(File_inputContext ctx) {
        Python3VariableExtractor extractor = new Python3VariableExtractor();
        this.variables.addAll(extractor.visitFile_input(ctx));
    }

    private void extractControls(File_inputContext ctx){
        Python3ControlExtractor extractor = new Python3ControlExtractor();
        this.controls.addAll(extractor.visitFile_input(ctx));
    }

    private void extractClasses(File_inputContext ctx) { 
        Python3ClassExtractor extractor = new Python3ClassExtractor();
        this.classes.addAll(extractor.visitFile_input(ctx));
    }

    private void extractModules(File_inputContext ctx) {
        Python3ModuleExtractor extractor = new Python3ModuleExtractor(path);
        this.modules.addAll(extractor.visitFile_input(ctx));
    }

    private void extractPackages() {
        for (Python3ModuleElement module : modules) {
            PackageElement packageElement = module.getPackage();
            if (!packages.contains(packageElement)) {
                packages.add(packageElement);
            }
        }
        packages.sort(Comparator.comparingInt(p -> p.getPackageNameParts("/").length));
    }
}
