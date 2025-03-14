package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementManager;

public class ModelMapper {
    protected final CodeItemRepository codeItemRepository;
    protected final ElementManager elementManager;
    protected final CodeItemBuilder codeItemBuilder;
    private CodeModel codeModel;

    public ModelMapper(CodeItemRepository codeItemRepository, CodeItemBuilder codeItemBuilder, ElementManager elementManager) {
        this.codeItemRepository = codeItemRepository;
        this.elementManager = elementManager;
        this.codeItemBuilder = codeItemBuilder;
    }

    public CodeModel getCodeModel() {
        return codeModel;
    }

    public void mapToCodeModel() {
        List<ElementIdentifier> rootParents = elementManager.getRootParents();
        SortedSet<CodeItem> content = buildContentFromRoot(rootParents);
        codeModel = new CodeModel(codeItemRepository, content);
    }

    protected SortedSet<CodeItem> buildContentFromRoot(List<ElementIdentifier> parents) {
        SortedSet<CodeItem> content = new TreeSet<>();

        for (ElementIdentifier parent : parents) {
            content.add(buildSubtree(parent));
        }
        return content;
    }

    protected CodeItem buildSubtree(ElementIdentifier parent) {
        Element element = elementManager.getElement(parent);
        return buildCodeItem(element);
    }

    private CodeItem buildCodeItem(Element element) {
        return this.codeItemBuilder.buildCodeItem(element);
    }
}
