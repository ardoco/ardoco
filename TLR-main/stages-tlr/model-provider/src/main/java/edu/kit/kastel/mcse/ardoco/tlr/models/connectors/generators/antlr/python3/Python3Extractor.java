package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.python3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ModuleElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3VariableElement;
import generated.antlr.Python3Lexer;
import generated.antlr.Python3Parser;
import generated.antlr.Python3Parser.File_inputContext;

public class Python3Extractor extends ANTLRExtractor {
    // private ProgrammingLanguage language = ProgrammingLanguage.PYTHON3;
    private List<Python3VariableElement> variables = new ArrayList<>();
    private List<ControlElement> controls = new ArrayList<>();
    private List<Python3ClassElement> classes = new ArrayList<>();
    private List<Python3ModuleElement> modules = new ArrayList<>();
    // private PythonModelMapper mapper = new PythonModelMapper();
    private CodeModel codeModel;


    public Python3Extractor(CodeItemRepository repository, String path) {
        super(repository, path);
    }

    public void execute() throws IOException {
        List<Path> files = getPythonFiles();
        for (Path file : files) {
            File_inputContext ctx = buildFileInputContext(file);
            extractElementsFromFile(ctx);
        }
    }

    public void mapToCodeModel() {
        // this.mapper = new PythonModelMapper(variables, controls, classes);
    }

    public List<Python3VariableElement> getVariables() {
        return variables;
    }

    public List<ControlElement> getControls() {
        return controls;
    }

    public List<Python3ClassElement> getClasses() {
        return classes;
    }

    public List<Python3ModuleElement> getModules() {
        return modules;
    }

    private List<Path> getPythonFiles() throws IOException {
        Path dir = Path.of(path);
        List<Path> pythonFiles = new ArrayList<>();
        Files.walk(dir)
        .filter(Files::isRegularFile)
        .filter(f -> f.toString().endsWith(".py"))
        .forEach(pythonFiles::add);
        return pythonFiles;
    }

    private File_inputContext buildFileInputContext(Path file) throws IOException {
        Python3Lexer lexer = new Python3Lexer(CharStreams.fromPath(file));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        return parser.file_input();
    }

    private void extractElementsFromFile(File_inputContext ctx) {
        if (ctx != null) {
            extractVariables(ctx);
            extractControls(ctx);
            extractClasses(ctx);
            extractModules(ctx);
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
}
