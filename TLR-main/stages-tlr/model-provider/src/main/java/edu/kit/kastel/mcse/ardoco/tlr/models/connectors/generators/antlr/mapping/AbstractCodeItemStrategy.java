package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;

public abstract class AbstractCodeItemStrategy implements CodeItemBuilderStrategy {
    protected final CodeItemRepository codeItemRepository;
    protected final CodeItemBuilder builder;

    protected AbstractCodeItemStrategy(CodeItemRepository repository, CodeItemBuilder builder) {
        this.codeItemRepository = repository;
        this.builder = builder;
    }

    @Override
    public abstract CodeItem buildCodeItem(Element element);

    @Override
    public abstract boolean supports(Element element);

    protected SortedSet<CodeItem> buildContent(ElementIdentifier parent) {
        SortedSet<CodeItem> content = new TreeSet<>();
        List<Element> elements = getContentOfParent(parent);
        for (Element element : elements) {
            CodeItem codeItem = buildCodeItemFromStrategy(element);
            if (codeItem != null) {
                content.add(codeItem);
            }
        }
        return content;
    }

    protected abstract List<Element> getContentOfParent(ElementIdentifier parent);

    protected CodeItem buildCodeItemFromStrategy(Element element) {
        return builder.buildCodeItem(element);
    }
    
}
