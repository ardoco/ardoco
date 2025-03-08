package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.cpp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.ANTLRExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction.CommentExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppModelMapper;
import generated.antlr.cpp.CPP14Lexer;
import generated.antlr.cpp.CPP14Parser;
import generated.antlr.cpp.CPP14Parser.TranslationUnitContext;

public class CppExtractor extends ANTLRExtractor {
    private final ProgrammingLanguage language = ProgrammingLanguage.CPP;
    private final CppElementExtractor elementExtractor;

    public CppExtractor(CodeItemRepository repository, String path) {
        super(repository, path);
        this.elementExtractor = new CppElementExtractor();
    }

    @Override
    protected List<Path> getFiles() {
        Path dir = Path.of(path);
        List<Path> cppFiles = new ArrayList<>();
        try {
            Files.walk(dir)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".cpp") || f.toString().endsWith(".h"))
                    .forEach(cppFiles::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cppFiles;
    }

    @Override
    protected void extractElementsFromFile(Path file) {
        try {
            TranslationUnitContext ctx = buildTranslationUnitContext(file);
            extractElementsFromContext(ctx);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void mapToCodeModel() {
        if (!elementsExtracted) {
            throw new IllegalStateException("Elements have not been extracted yet.");
        }
        CppElementManager elementManager = elementExtractor.getElementManager();
        elementManager.addComments(comments);
        this.mapper = new CppModelMapper(this.codeItemRepository, elementManager);
    }

    @Override
    protected CommentExtractor createCommentExtractor(CommonTokenStream tokens, String path) {
        return new CppCommentExtractor(tokens, path);
    }

    public CppElementManager getElementManager() {
        return this.elementExtractor.getElementManager();
    }

    private TranslationUnitContext buildTranslationUnitContext(Path file) throws IOException {
        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromPath(file));
        this.tokens = new CommonTokenStream(lexer);
        CPP14Parser parser = new CPP14Parser(tokens);
        return parser.translationUnit();
    }

    private void extractElementsFromContext(TranslationUnitContext ctx) {
        if (ctx == null) {
            return;
        }
        this.elementExtractor.extract(ctx);
    }
}
