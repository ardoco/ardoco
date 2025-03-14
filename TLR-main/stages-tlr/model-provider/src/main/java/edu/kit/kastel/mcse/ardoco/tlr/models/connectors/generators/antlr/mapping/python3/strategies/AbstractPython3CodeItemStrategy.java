package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.strategies;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.AbstractCodeItemStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemBuilder;

public abstract class AbstractPython3CodeItemStrategy extends AbstractCodeItemStrategy{
    protected final Python3ElementManager elementManager;

    protected AbstractPython3CodeItemStrategy(CodeItemRepository repository, CodeItemBuilder codeItemBuilder, Python3ElementManager elementManager) {
        super(repository, codeItemBuilder);
        this.elementManager = elementManager;
    }

    @Override
    protected List<Element> getContentOfParent(ElementIdentifier parent) {
        return elementManager.getContentOfParent(parent);
    }
    
}
