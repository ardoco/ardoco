/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping;

import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jspecify.annotations.Nullable;

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
    private @Nullable CodeModel codeModel;

    public ModelMapper(CodeItemRepository codeItemRepository, CodeItemMapperCollection codeItemMappers, ElementStorageRegistry elementRegistry) {
        this.codeItemRepository = codeItemRepository;
        this.elementRegistry = elementRegistry;
        this.codeItemMappers = codeItemMappers;
    }

    public CodeModel getCodeModel() {
        return Objects.requireNonNull(codeModel, "The code model has not been created yet");
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
        Element element = Objects.requireNonNull(elementRegistry.getElement(identifier), () -> "No element found for identifier " + identifier);
        return buildCodeItem(element);
    }

    private CodeItem buildCodeItem(Element element) {
        return Objects.requireNonNull(this.codeItemMappers.buildCodeItem(element), () -> "No code item could be built for element " + element.getName());
    }
}
