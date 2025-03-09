package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.JavaElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemBuilder;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.strategies.ClassStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.strategies.CompilationUnitStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.strategies.FunctionStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.strategies.InterfaceStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.strategies.PackageStrategy;

public class JavaCodeItemBuilder extends CodeItemBuilder {

    public JavaCodeItemBuilder(CodeItemRepository repository, JavaElementManager elementManager) {
        super();
        this.strategies = List.of(
            new FunctionStrategy(repository, this, elementManager), 
            new ClassStrategy(repository, this, elementManager),
            new InterfaceStrategy(repository, this, elementManager),
            new CompilationUnitStrategy(repository, this, elementManager),
            new PackageStrategy(repository, this, elementManager)
        );
    }
    
}
