package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;

public abstract class CodeItemMapperCollection {
    protected List<CodeItemMapper> mappers;
    
    protected CodeItemMapperCollection() {}

    public CodeItem buildCodeItem(Element element) {
        return mappers.stream()
            .filter(strategy -> strategy.supports(element))
            .findFirst()
            .map(strategy -> strategy.buildCodeItem(element))
            .orElse(null);
    }
}
