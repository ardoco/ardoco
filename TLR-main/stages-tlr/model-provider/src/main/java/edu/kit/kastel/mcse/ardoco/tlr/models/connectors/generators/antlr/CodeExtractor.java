package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.Extractor;

public abstract class CodeExtractor extends Extractor {
    protected final CodeItemRepository codeItemRepository;

    protected CodeExtractor(CodeItemRepository codeItemRepository, String path) {
        super(path);
        this.codeItemRepository = codeItemRepository;
    }

    @Override
    public abstract CodeModel extractModel();

    @Override
    public final ModelType getModelType() {
        return CodeModelType.CODE_MODEL;
    }
}