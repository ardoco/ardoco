/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.ElementStorageRegistry;

/**
 * Responsible for mapping extracted elements to the Arcotl model.
 * Uses a CodeItemMapperCollection to map elements to CodeItems.
 */
public class ModelMapper {
    protected final CodeItemRepository codeItemRepository;
    protected final ElementStorageRegistry elementRegistry;
    protected final CodeItemMapperCollection codeItemMappers;
    private CodeModel codeModel;

    public ModelMapper(CodeItemRepository codeItemRepository, CodeItemMapperCollection codeItemMappers, ElementStorageRegistry elementRegistry) {
        this.codeItemRepository = codeItemRepository;
        this.elementRegistry = elementRegistry;
        this.codeItemMappers = codeItemMappers;
    }

    public CodeModel getCodeModel() {
        return codeModel;
    }

    public void mapToCodeModel() {
        List<ElementIdentifier> rootParentIdentifiers = elementRegistry.getRootIdentifiers();
        SortedSet<CodeItem> content = buildContentFromRoot(rootParentIdentifiers);
        // TODO Allow different meta models
        codeModel = new CodeModelWithCompilationUnits(codeItemRepository, content);
    }

    protected SortedSet<CodeItem> buildContentFromRoot(List<ElementIdentifier> identifiers) {
        SortedSet<CodeItem> content = new TreeSet<>();

        for (ElementIdentifier identifier : identifiers) {
            content.add(buildSubtree(identifier));
        }
        return content;
    }

    protected CodeItem buildSubtree(ElementIdentifier identifier) {
        Element element = elementRegistry.getElement(identifier);
        return buildCodeItem(element);
    }

    private CodeItem buildCodeItem(Element element) {
        return this.codeItemMappers.buildCodeItem(element);
    }
}
