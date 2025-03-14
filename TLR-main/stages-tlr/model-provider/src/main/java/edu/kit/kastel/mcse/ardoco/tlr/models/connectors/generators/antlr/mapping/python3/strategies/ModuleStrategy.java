package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.strategies;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.CodeItemBuilder;

public class ModuleStrategy extends AbstractPython3CodeItemStrategy{

    public ModuleStrategy(CodeItemRepository repository, CodeItemBuilder codeItemBuilder, Python3ElementManager elementManager) {
        super(repository, codeItemBuilder, elementManager);
    }

    @Override
    public boolean supports(Element element) {
        return elementManager.isModuleElement(element);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.MODULE);
        return buildCodeAssembly(comparable);
    }


    private CodeAssembly buildCodeAssembly(ElementIdentifier parent) {
        Element module = elementManager.getModule(parent);
        SortedSet<CodeItem> content = buildContent(parent);
        CodeAssembly codeAssembly = new CodeAssembly(codeItemRepository, module.getName(), content);
        codeAssembly.setComment(module.getComment());
        return codeAssembly;
    }
    
}
