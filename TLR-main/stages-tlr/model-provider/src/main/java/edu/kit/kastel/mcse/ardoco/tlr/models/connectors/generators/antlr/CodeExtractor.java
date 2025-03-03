package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.Extractor;

public abstract class CodeExtractor extends Extractor {
    private static final Logger logger = LoggerFactory.getLogger(CodeExtractor.class);

    private static final String CODE_MODEL_FILE_NAME = "codeModel.acm";
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