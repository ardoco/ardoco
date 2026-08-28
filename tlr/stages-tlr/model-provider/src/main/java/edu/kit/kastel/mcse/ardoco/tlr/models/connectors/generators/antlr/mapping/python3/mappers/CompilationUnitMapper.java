/* Licensed under MIT 2025-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.mappers;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.Datatype;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3.Python3ElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemMapperCollection;

/**
 * Responsible for mapping a Python3 ModuleElement to a CodeCompilationUnit
 */
public class CompilationUnitMapper extends AbstractPython3CodeItemMapper {

    public CompilationUnitMapper(CodeItemRepository repository, CodeItemMapperCollection pythonCodeItemMappers, Python3ElementStorageRegistry elementRegistry) {
        super(repository, pythonCodeItemMappers, elementRegistry);
    }

    @Override
    public boolean supports(Element element) {
        return elementRegistry.isModuleElement(element);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.MODULE);
        return buildCodeCompilationUnit(comparable);
    }

    private CodeCompilationUnit buildCodeCompilationUnit(ElementIdentifier identifier) {
        Element module = elementRegistry.getModule(identifier);
        SortedSet<CodeItem> content = buildContent(identifier);

        CodeCompilationUnit unit = CodeCompilationUnit.fromRelativePath(codeItemRepository, content, this.language, module.getPath(), module.getImports());
        unit.setComment(module.getComment());
        for (Datatype type : unit.getAllDataTypes()) {
            type.setCompilationUnit(unit);
        }
        return unit;
    }

}
