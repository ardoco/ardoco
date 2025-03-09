package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.CodeExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public abstract class ANTLRExtractor extends CodeExtractor {
    public final ProgrammingLanguage LANGUAGE;
    protected ModelMapper mapper;
    protected ElementExtractor elementExtractor;
    protected ElementManager elementManager;
    protected CommentExtractor commentExtractor;
    protected boolean contentExtracted;

    protected ANTLRExtractor(CodeItemRepository codeItemRepository, String path, ProgrammingLanguage language) {
        super(codeItemRepository, path);
        this.LANGUAGE = language;
        contentExtracted = false;
    }

    @Override
    public synchronized CodeModel extractModel() {
        if (!contentExtracted) {
            extractContent();
            contentExtracted = true;
        }
        mapToCodeModel();
        return this.mapper.getCodeModel();
    }

    public ElementExtractor getElementExtractor() {
        return elementExtractor;
    }

    public ModelMapper getMapper() {
        return mapper;
    }

    protected abstract List<Path> getFiles();

    protected abstract CommonTokenStream buildTokens(Path file) throws IOException;

    private void mapToCodeModel() {
        if (contentExtracted) {
            this.mapper.mapToCodeModel();
        }
    }

    private void extractContent() {
        List<Path> files = getFiles();
        for (Path file : files) {
            CommonTokenStream tokens;
            try {
                tokens = buildTokens(file);
                
            } catch (IOException e) {
                continue;
            }
            extractContentFromTokens(file, tokens);
        }
    }

    private void extractContentFromTokens(Path file, CommonTokenStream tokenStream) {
        extractElementsFromFile(tokenStream);
        extractComments(file, tokenStream);
    }

    private void extractElementsFromFile(CommonTokenStream tokenStream) {
        this.elementExtractor.extract(tokenStream);
    }

    private void extractComments(Path file, CommonTokenStream tokens) {
        String path = file.toString();
        commentExtractor.extract(path, tokens);
    }

}
