package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.strategies;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.AbstractCodeItemStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemBuilder;

public abstract class AbstractCppCodeItemStrategy extends AbstractCodeItemStrategy {
    protected final CppElementStorageRegistry elementManager;

    protected AbstractCppCodeItemStrategy(CodeItemRepository repository, CodeItemBuilder codeItemBuilder, CppElementStorageRegistry elementManager) {
        super(repository, codeItemBuilder);
        this.elementManager = elementManager;
    }

    @Override
    protected List<Element> getContentOfParent(ElementIdentifier parent) {
        return elementManager.getContentOfParent(parent);
    }
    
}
