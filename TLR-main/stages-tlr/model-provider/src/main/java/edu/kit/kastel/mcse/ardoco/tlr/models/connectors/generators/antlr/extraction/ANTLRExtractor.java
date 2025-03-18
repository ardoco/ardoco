package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.extraction;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.CodeExtractor;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public class ANTLRExtractor extends CodeExtractor {
    private final ProgrammingLanguage language;
    protected ModelMapper mapper;
    protected ElementExtractor elementExtractor;
    private boolean contentExtracted;

    protected ANTLRExtractor(CodeItemRepository codeItemRepository, String path, ProgrammingLanguage language) {
        super(codeItemRepository, path);
        this.language = language;
        this.contentExtracted = false;
    }

    @Override
    public synchronized CodeModel extractModel() {
        if (!contentExtracted) {
            extractContent();
            mapToCodeModel();
            contentExtracted = true;
        }
        return this.mapper.getCodeModel();
    }

    public ElementExtractor getElementExtractor() {
        return elementExtractor;
    }

    public ProgrammingLanguage getLanguage() {
        return language;
    }

    public ModelMapper getMapper() {
        return mapper;
    }

    private void mapToCodeModel() {
        this.mapper.mapToCodeModel();
    }

    private void extractContent() {
        this.elementExtractor.extract(this.path);
    }
}
