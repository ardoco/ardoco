package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public class CppModelMapper extends ModelMapper {

    public CppModelMapper(CodeItemRepository codeItemRepository, CppElementManager elementManager) {
        super(codeItemRepository, new CppCodeItemBuilder(codeItemRepository, elementManager), elementManager);
    }
}
