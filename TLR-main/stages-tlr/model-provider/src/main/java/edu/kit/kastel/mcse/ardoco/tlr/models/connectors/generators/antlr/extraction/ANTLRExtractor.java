package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.CodeExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Comment;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public abstract class ANTLRExtractor extends CodeExtractor {
    protected ModelMapper mapper;
    protected CommonTokenStream tokens;
    protected boolean elementsExtracted;
    protected final List<Comment> comments;

    protected ANTLRExtractor(CodeItemRepository codeItemRepository, String path) {
        super(codeItemRepository, path);
        elementsExtracted = false;
        this.comments = new ArrayList<>();
    }

    @Override
    public synchronized CodeModel extractModel() {
        if (!elementsExtracted) {
            extractElements();
        }
        mapToCodeModel();
        return this.mapper.getCodeModel();
    }

    // public for Test purposes
    public void extractElements() {
        List<Path> files = getFiles();
        for (Path file : files) {
            extractContentFromFile(file);
        }
        this.elementsExtracted = true;
    }

    public ModelMapper getMapper() {
        return mapper;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void extractComments(Path file) {
        String path = file.toString();
        CommentExtractor commentExtractor = createCommentExtractor(tokens, path);
        commentExtractor.extract();
        comments.addAll(commentExtractor.getComments());
    }

    protected void extractContentFromFile(Path file) {
        extractElementsFromFile(file);
        extractComments(file);
    }

    protected abstract List<Path> getFiles();

    protected abstract void extractElementsFromFile(Path file);

    protected abstract void mapToCodeModel();

    protected abstract CommentExtractor createCommentExtractor(CommonTokenStream tokens, String path);
}
