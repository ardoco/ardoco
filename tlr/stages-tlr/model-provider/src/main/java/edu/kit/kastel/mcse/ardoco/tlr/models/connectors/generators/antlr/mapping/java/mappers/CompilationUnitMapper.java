/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.mappers;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.Datatype;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaCodeItemMapperCollection;

/**
 * Responsible for mapping a Java CompilationUnit to a CodeCompilationUnit.
 */
public class CompilationUnitMapper extends AbstractJavaCodeItemMapper {

    public CompilationUnitMapper(CodeItemRepository codeItemRepository, JavaCodeItemMapperCollection javaCodeItemMappers,
            JavaElementStorageRegistry elementRegistry) {
        super(codeItemRepository, javaCodeItemMappers, elementRegistry);
    }

    @Override
    public boolean supports(Element element) {
        return this.elementRegistry.isCompilationUnitElement(element);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.COMPILATIONUNIT);
        return buildCodeCompilationUnit(comparable);
    }

    private CodeCompilationUnit buildCodeCompilationUnit(ElementIdentifier identifier) {
        Element compilationUnit = elementRegistry.getCompilationUnitElement(identifier);
        String[] allParts = compilationUnit.getPath().split("/");
        List<String> pathElements = Arrays.asList(allParts).subList(0, allParts.length - 1);
        SortedSet<CodeItem> content = buildContent(identifier);

        String fileName = allParts[allParts.length - 1];
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1) : "";
        CodeCompilationUnit codeCompilationUnit = new CodeCompilationUnit(codeItemRepository, compilationUnit.getName(), content, pathElements, extension,
                this.language, compilationUnit.getImports());
        codeCompilationUnit.setComment(compilationUnit.getComment());
        for (Datatype type : codeCompilationUnit.getAllDataTypes()) {
            type.setCompilationUnit(codeCompilationUnit);
        }
        return codeCompilationUnit;
    }

}
