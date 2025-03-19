package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.mappers;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.AbstractCodeItemMapper;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaCodeItemMapperCollection;

public abstract class AbstractJavaCodeItemMapper extends AbstractCodeItemMapper {
    protected final JavaElementStorageRegistry elementManager;

    protected AbstractJavaCodeItemMapper(CodeItemRepository codeItemRepository, JavaCodeItemMapperCollection javaCodeItemBuilder, JavaElementStorageRegistry elementManager) {
        super(codeItemRepository, javaCodeItemBuilder);
        this.elementManager = elementManager;
    }

    @Override 
    protected List<Element> getContentOfParent(ElementIdentifier parent) {
        return elementManager.getContentOfParent(parent);
    }
    
}