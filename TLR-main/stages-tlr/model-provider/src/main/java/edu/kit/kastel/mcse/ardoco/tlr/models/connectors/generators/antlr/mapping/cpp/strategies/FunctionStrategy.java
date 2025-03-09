package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.strategies;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppCodeItemBuilder;

public class FunctionStrategy extends AbstractCppCodeItemStrategy {

    public FunctionStrategy(CodeItemRepository codeItemRepository, CppCodeItemBuilder cppCodeItemBuilder, CppElementManager elementManager) {
        super(codeItemRepository, cppCodeItemBuilder, elementManager);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        Parent comparable = new Parent(element.getName(), element.getPath(), Type.FUNCTION);
        return buildControlElement(comparable);
    }

    @Override
    public boolean supports(Element element) {
        return elementManager.isFunctionElement(element);
    }

    private CodeItem buildControlElement(Parent parent) {
        Element function = this.elementManager.getFunction(parent);

        ControlElement controlElement = new ControlElement(codeItemRepository, function.getName());
        controlElement.setComment(function.getComment());
        return controlElement;
    }
    
}
