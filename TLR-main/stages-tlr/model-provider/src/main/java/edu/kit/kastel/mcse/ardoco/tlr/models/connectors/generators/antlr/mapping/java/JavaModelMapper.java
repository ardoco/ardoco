package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public class JavaModelMapper extends ModelMapper {

    public JavaModelMapper(CodeItemRepository codeItemRepository, JavaElementStorageRegistry elementManager) {
        super(codeItemRepository, new JavaCodeItemMapperCollection(codeItemRepository, elementManager), elementManager);
    }
}
