package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemMapperCollection;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.mappers.ClassMapper;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.mappers.FileMapper;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.mappers.FunctionMapper;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.mappers.NamespaceMapper;

public class CppCodeItemMapperCollection extends CodeItemMapperCollection{
    
    public CppCodeItemMapperCollection(CodeItemRepository repository, CppElementStorageRegistry elementManager) {
        super();
        this.mappers = List.of(
            new FunctionMapper(repository, this, elementManager), 
            new ClassMapper(repository, this, elementManager),
            new NamespaceMapper(repository, this, elementManager),
            new FileMapper(repository, this, elementManager));
    }    
}
