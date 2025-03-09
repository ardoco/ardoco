package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.CodeExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.management.ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public abstract class ANTLRExtractor extends CodeExtractor {
    public final ProgrammingLanguage LANGUAGE;
    protected ModelMapper mapper;
    protected ElementExtractor elementExtractor;
    protected ElementManager elementManager;
    protected boolean contentExtracted;
    protected final List<Comment> comments;

    protected ANTLRExtractor(CodeItemRepository codeItemRepository, String path, ProgrammingLanguage language) {
        super(codeItemRepository, path);
        this.LANGUAGE = language;
        contentExtracted = false;
        this.comments = new ArrayList<>();
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

    public List<Comment> getComments() {
        return comments;
    }

    protected abstract List<Path> getFiles();

    protected abstract CommentExtractor createCommentExtractor(CommonTokenStream tokens, String path);

    private void mapToCodeModel() {
        if (contentExtracted) {
            this.mapper.mapToCodeModel();
        }
    }

    private void extractContent() {
        List<Path> files = getFiles();
        for (Path file : files) {
            extractContentFromFile(file);
        }
        addCommentsToExtractedElements();
    }

    private void extractContentFromFile(Path file) {
        extractElementsFromFile(file);
        extractComments(file);
    }

    private void extractElementsFromFile(Path file) {
        this.elementExtractor.extract(file);
    }

    private void extractComments(Path file) {
        String path = file.toString();
        CommonTokenStream tokens = elementExtractor.getTokens();
        CommentExtractor commentExtractor = createCommentExtractor(tokens, path);
        commentExtractor.extract();
        comments.addAll(commentExtractor.getComments());
    }

    private void addCommentsToExtractedElements() {
        this.elementManager.addComments(comments);
    }

}
