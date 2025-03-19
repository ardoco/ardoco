package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3.Python3ElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemMapperCollection;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.mappers.*;

public class Python3CodeItemMapperCollection extends CodeItemMapperCollection {

    public Python3CodeItemMapperCollection(CodeItemRepository repository, Python3ElementStorageRegistry elementManager) {
        super();
        this.mappers = List.of(
            new FunctionMapper(repository, this, elementManager), 
            new ClassMapper(repository, this, elementManager),
            new ModuleMapper(repository, this, elementManager),
            new PackageMapper(repository, this, elementManager)
        );
    }
    
}
