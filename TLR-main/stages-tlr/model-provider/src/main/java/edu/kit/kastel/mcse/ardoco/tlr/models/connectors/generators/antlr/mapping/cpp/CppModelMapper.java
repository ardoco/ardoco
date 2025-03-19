package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public class CppModelMapper extends ModelMapper {

    public CppModelMapper(CodeItemRepository codeItemRepository, CppElementStorageRegistry elementManager) {
        super(codeItemRepository, new CppCodeItemMapperCollection(codeItemRepository, elementManager), elementManager);
    }
}
