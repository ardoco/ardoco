package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;

public abstract class CodeItemBuilder {
    protected List<CodeItemBuilderStrategy> strategies;
    
    protected CodeItemBuilder() {}

    public CodeItem buildCodeItem(Element element) {
        return strategies.stream()
            .filter(strategy -> strategy.supports(element))
            .findFirst()
            .map(strategy -> strategy.buildCodeItem(element))
            .orElse(null);
    }
}
