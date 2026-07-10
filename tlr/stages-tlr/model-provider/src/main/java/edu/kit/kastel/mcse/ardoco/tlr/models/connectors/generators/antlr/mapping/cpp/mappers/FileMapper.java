/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.mappers;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppCodeItemMapperCollection;

/**
 * Responsible for mapping a C++ File to a CodeAssembly.
 */
public class FileMapper extends AbstractCppCodeItemMapper {

    public FileMapper(CodeItemRepository codeItemRepository, CppCodeItemMapperCollection cppCodeItemMappers, CppElementStorageRegistry elementRegistry) {
        super(codeItemRepository, cppCodeItemMappers, elementRegistry);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.FILE);
        return buildFileCodeAssembly(comparable);
    }

    @Override
    public boolean supports(Element element) {
        return this.elementRegistry.isFileElement(element);
    }

    private CodeItem buildFileCodeAssembly(ElementIdentifier identifier) {
        Element file = this.elementRegistry.getFile(identifier);
        SortedSet<CodeItem> content = buildContent(identifier);
        String path = file.getPath();
        int lastSlash = path.lastIndexOf('/');
        List<String> pathElements = lastSlash >= 0 ? Arrays.asList(path.substring(0, lastSlash).split("/")) : List.of();
        String extension = path.substring(path.lastIndexOf('.') + 1);
        return new CodeAssembly(this.codeItemRepository, file.getName(), content, this.language.name(), pathElements, extension, file.getImports());
    }
}
