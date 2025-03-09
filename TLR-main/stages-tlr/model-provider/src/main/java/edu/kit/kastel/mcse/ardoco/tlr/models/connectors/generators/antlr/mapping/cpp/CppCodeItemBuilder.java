package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemBuilder;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.strategies.ClassStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.strategies.FileStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.strategies.FunctionStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.strategies.NamespaceStrategy;

public class CppCodeItemBuilder extends CodeItemBuilder{
    
    public CppCodeItemBuilder(CodeItemRepository repository, CppElementManager elementManager) {
        super();
        this.strategies = List.of(
            new FunctionStrategy(repository, this, elementManager), 
            new ClassStrategy(repository, this, elementManager),
            new NamespaceStrategy(repository, this, elementManager),
            new FileStrategy(repository, this, elementManager));
    }    
}
