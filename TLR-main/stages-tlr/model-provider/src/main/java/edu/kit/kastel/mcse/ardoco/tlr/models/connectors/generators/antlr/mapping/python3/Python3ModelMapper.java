package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public class Python3ModelMapper extends ModelMapper{

    public Python3ModelMapper(CodeItemRepository codeItemRepository, Python3ElementManager elementManager) {
        super(codeItemRepository, new Python3CodeItemBuilder(codeItemRepository, elementManager), elementManager);
    }
}