package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.strategies;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.JavaElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaCodeItemBuilder;

public class FunctionStrategy extends AbstractJavaCodeItemStrategy {

    public FunctionStrategy(CodeItemRepository codeItemRepository, JavaCodeItemBuilder javaCodeItemBuilder, JavaElementManager elementManager) {
        super(codeItemRepository, javaCodeItemBuilder, elementManager);
    }

    @Override
    public boolean supports(Element element) {
        return elementManager.isFunctionElement(element);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        Parent comparable = new Parent(element.getName(), element.getPath(), Type.FUNCTION);
        return buildControlElement(comparable);
    }

    private CodeItem buildControlElement(Parent parent) {
        Element function = this.elementManager.getFunction(parent);

        ControlElement controlElement = new ControlElement(codeItemRepository, function.getName());
        controlElement.setComment(function.getComment());
        return controlElement;
    }
    
}
