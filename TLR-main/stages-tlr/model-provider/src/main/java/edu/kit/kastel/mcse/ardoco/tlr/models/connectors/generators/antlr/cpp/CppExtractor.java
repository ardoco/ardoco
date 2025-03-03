package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.cpp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp.NamespaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mappers.CppModelMapper;
import generated.antlr.cpp.CPP14Lexer;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14Parser.TranslationUnitContext;

public class CppExtractor extends ANTLRExtractor {
    private List<VariableElement> variables = new ArrayList<>();
    private List<ControlElement> controls = new ArrayList<>();
    private List<ClassElement> classes = new ArrayList<>();
    private List<NamespaceElement> namespaces = new ArrayList<>();
    private List<CommentElement> comments = new ArrayList<>();
    private CommonTokenStream tokens;
    private CppModelMapper mapper;
    private boolean extracted;

    public CppExtractor(CodeItemRepository repository, String path) {
        super(repository, path);
    }

    public void execute() throws IOException {
        List<Path> files = getCppFiles();
        for (Path file : files) {
            TranslationUnitContext ctx = buildTranslationUnitContext(file);
            extractElementsFromFile(ctx, file);
        }
    }

    public void mapToCodeModel() {
        if (!extracted) {
            throw new IllegalStateException("Elements have not been extracted yet.");
        }
        this.mapper = new CppModelMapper(this.codeItemRepository, variables, controls, classes, namespaces, comments);
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

    public List<NamespaceElement> getNamespaces() {
        return namespaces;
    }

    public List<CommentElement> getComments() {
        return comments;
    }

    public CppModelMapper getMapper() {
        return mapper;
    }

    private List<Path> getCppFiles() throws IOException {
        Path dir = Path.of(path);
        List<Path> cppFiles = new ArrayList<>();
        Files.walk(dir)
        .filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(".cpp") || p.toString().endsWith(".h"))
        .forEach(cppFiles::add);
        return cppFiles;
    }

    private TranslationUnitContext buildTranslationUnitContext(Path file) throws IOException {
        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromPath(file));
        this.tokens = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokens);
        return parser.translationUnit();
    }

    private void extractElementsFromFile(TranslationUnitContext ctx, Path file) {
        if (ctx == null) {
            return;
        }
        extractVariables(ctx);
        extractControls(ctx);
        extractClasses(ctx);
        extractNamespaces(ctx);
        extractComments(file);
        extracted = true;
    }

    private void extractVariables(TranslationUnitContext ctx) {
        CppVariableExtractor extractor = new CppVariableExtractor();
        this.variables.addAll(extractor.visitTranslationUnit(ctx));
    }

    private void extractControls(TranslationUnitContext ctx) {
        CppControlExtractor extractor = new CppControlExtractor();
        this.controls.addAll(extractor.visitTranslationUnit(ctx));
    }

    private void extractClasses(TranslationUnitContext ctx) {
        CppClassExtractor extractor = new CppClassExtractor();
        this.classes.addAll(extractor.visitTranslationUnit(ctx));
    }

    private void extractNamespaces(TranslationUnitContext ctx) {
        CppNamespaceExtractor extractor = new CppNamespaceExtractor();
        this.namespaces.addAll(extractor.visitTranslationUnit(ctx));
    }

    private void extractComments(Path file) {
        String path = file.toString();
        CppCommentExtractor extractor = new CppCommentExtractor(tokens, path);
        extractor.extract();
        this.comments.addAll(extractor.getComments());
    }


    
    
    
}
