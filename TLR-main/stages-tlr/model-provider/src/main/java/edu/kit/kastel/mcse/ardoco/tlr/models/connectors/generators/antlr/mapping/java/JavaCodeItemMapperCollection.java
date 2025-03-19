package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemMapperCollection;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.mappers.ClassMapper;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.mappers.CompilationUnitMapper;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.mappers.FunctionMapper;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.mappers.InterfaceMapper;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.mappers.PackageMapper;

public class JavaCodeItemMapperCollection extends CodeItemMapperCollection {

    public JavaCodeItemMapperCollection(CodeItemRepository repository, JavaElementStorageRegistry elementManager) {
        super();
        this.mappers = List.of(
            new FunctionMapper(repository, this, elementManager), 
            new ClassMapper(repository, this, elementManager),
            new InterfaceMapper(repository, this, elementManager),
            new CompilationUnitMapper(repository, this, elementManager),
            new PackageMapper(repository, this, elementManager)
        );
    }
    
}
