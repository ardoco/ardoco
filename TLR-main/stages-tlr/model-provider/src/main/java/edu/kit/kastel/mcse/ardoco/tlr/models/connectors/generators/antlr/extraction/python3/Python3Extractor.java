package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.python3;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.Python3ModelMapper;
import generated.antlr.python3.Python3Lexer;
import generated.antlr.python3.Python3Parser;
import generated.antlr.python3.Python3Parser.File_inputContext;

public class Python3Extractor extends ANTLRExtractor {
    private static final ProgrammingLanguage LANGUAGE = ProgrammingLanguage.PYTHON3;
    private final Python3ElementExtractor elementExtractor;

    public Python3Extractor(CodeItemRepository repository, String path) {
        super(repository, path);
        this.elementExtractor = new Python3ElementExtractor();
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
    protected void extractElementsFromFile(Path file) {
        try {
            File_inputContext ctx = buildFileInputContext(file);
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
        Python3ElementManager elementManager = elementExtractor.getElementManager();
        elementManager.addComments(comments);
        this.mapper = new Python3ModelMapper(this.codeItemRepository, elementManager);
    }

    @Override
    protected CommentExtractor createCommentExtractor(CommonTokenStream tokens, String path) {
        return new Python3CommentExtractor(tokens, path);
    }

    // Visibility for Testing
    public Python3ElementManager getElementManager() {
        return this.elementExtractor.getElementManager();
    }

    private File_inputContext buildFileInputContext(Path file) throws IOException {
        Python3Lexer lexer = new Python3Lexer(CharStreams.fromPath(file));
        super.tokens = new CommonTokenStream(lexer);
        Python3Parser parser = new Python3Parser(tokens);
        return parser.file_input();
    }

    private void extractElements(File_inputContext ctx) {
        if (ctx == null) {
            return;
        }
        this.elementExtractor.extract(ctx);
    }
}
