package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.mappers;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppCodeItemMapperCollection;

public class NamespaceMapper extends AbstractCppCodeItemMapper {

    public NamespaceMapper(CodeItemRepository codeItemRepository, CppCodeItemMapperCollection cppCodeItemBuilder, CppElementStorageRegistry elementManager) {
        super(codeItemRepository, cppCodeItemBuilder, elementManager);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.NAMESPACE);
        return buildNamespaceCodeAssembly(comparable);
    }

    @Override
    public boolean supports(Element element) {
        return elementManager.isNamespaceElement(element);
    }

    private CodeItem buildNamespaceCodeAssembly(ElementIdentifier parent) {
        Element namespace = this.elementManager.getNamespace(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        CodeAssembly codeAssembly = new CodeAssembly(codeItemRepository, namespace.getName(), content);
        codeAssembly.setComment(namespace.getComment());
        return codeAssembly;
    }
    
}
