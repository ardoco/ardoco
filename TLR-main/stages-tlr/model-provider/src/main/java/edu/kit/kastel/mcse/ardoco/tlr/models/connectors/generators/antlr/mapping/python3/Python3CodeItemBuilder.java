package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemBuilder;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.strategies.*;

public class Python3CodeItemBuilder extends CodeItemBuilder {

    public Python3CodeItemBuilder(CodeItemRepository repository, Python3ElementManager elementManager) {
        super();
        this.strategies = List.of(
            new FunctionStrategy(repository, this, elementManager), 
            new ClassStrategy(repository, this, elementManager),
            new ModuleStrategy(repository, this, elementManager),
            new PackageStrategy(repository, this, elementManager)
        );
    }
    
}
