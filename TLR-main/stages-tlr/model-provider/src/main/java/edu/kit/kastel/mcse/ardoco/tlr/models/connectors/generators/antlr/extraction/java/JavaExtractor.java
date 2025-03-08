package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.JavaElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaModelMapper;
import generated.antlr.java.JavaLexer;
import generated.antlr.java.JavaParser;
import generated.antlr.java.JavaParser.CompilationUnitContext;

public class JavaExtractor extends ANTLRExtractor {
    private final ProgrammingLanguage language = ProgrammingLanguage.JAVA;
    private final JavaElementExtractor elementExtractor;

    public JavaExtractor(CodeItemRepository repository, String path) {
        super(repository, path);
        this.elementExtractor = new JavaElementExtractor();
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
    protected void extractElementsFromFile(Path file) {
        try {
            CompilationUnitContext ctx = buildCompilationCtxForFile(file);
            extractElements(ctx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void mapToCodeModel() {
        if (!elementsExtracted) {
            throw new IllegalStateException("Elements have not been extracted yet.");
        }
        JavaElementManager elementManager = elementExtractor.getElementManager();
        elementManager.addComments(comments);
        this.mapper = new JavaModelMapper(codeItemRepository, elementManager);
    }

    @Override
    protected CommentExtractor createCommentExtractor(CommonTokenStream tokens, String path) {
        return new JavaCommentExtractor(tokens, path);
    }

    public JavaElementManager getElementManager() {
        return this.elementExtractor.getElementManager();
    }

    private CompilationUnitContext buildCompilationCtxForFile(Path file) throws IOException {
        CharStream input = CharStreams.fromFileName(file.toString());
        JavaLexer lexer = new JavaLexer(input);
        this.tokens = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokens);
        return parser.compilationUnit();
    }

    private void extractElements(CompilationUnitContext ctx) {
        if (ctx == null) {
            return;
        }
        this.elementExtractor.extract(ctx);
    }
}
