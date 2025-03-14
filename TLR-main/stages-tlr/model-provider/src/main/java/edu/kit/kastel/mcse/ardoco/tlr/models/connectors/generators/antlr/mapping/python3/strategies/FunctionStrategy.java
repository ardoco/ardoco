package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.strategies;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.python3.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3.Python3CodeItemBuilder;

public class FunctionStrategy extends AbstractPython3CodeItemStrategy{

    public FunctionStrategy(CodeItemRepository codeItemRepository, Python3CodeItemBuilder python3CodeItemBuilder, Python3ElementManager python3ElementManager) {
        super(codeItemRepository, python3CodeItemBuilder, python3ElementManager);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.FUNCTION);
        return buildControlElement(comparable);
    }

    @Override
    public boolean supports(Element element) {
        return elementManager.isFunctionElement(element);
    }

    private CodeItem buildControlElement(ElementIdentifier parent) {
        Element function = this.elementManager.getFunction(parent);

        ControlElement controlElement = new ControlElement(codeItemRepository, function.getName());
        controlElement.setComment(function.getComment());
        return controlElement;
    }
    
}
