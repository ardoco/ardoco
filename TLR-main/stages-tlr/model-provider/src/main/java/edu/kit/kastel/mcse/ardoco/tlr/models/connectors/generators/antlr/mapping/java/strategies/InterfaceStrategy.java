package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.strategies;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.InterfaceUnit;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaCodeItemBuilder;

public class InterfaceStrategy extends AbstractJavaCodeItemStrategy {

    public InterfaceStrategy(CodeItemRepository codeItemRepository, JavaCodeItemBuilder javaCodeItemBuilder, JavaElementManager elementManager) {
        super(codeItemRepository, javaCodeItemBuilder, elementManager);
    }

    @Override
    public boolean supports(Element element) {
        return this.elementManager.isInterfaceElement(element);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.INTERFACE);
        return buildInterfaceUnit(comparable);
    }

    private InterfaceUnit buildInterfaceUnit(ElementIdentifier parent) {
        Element interfaceElement = elementManager.getInterface(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        InterfaceUnit interfaceUnit = new InterfaceUnit(codeItemRepository, interfaceElement.getName(), content);
        interfaceUnit.setComment(interfaceElement.getComment());
        return interfaceUnit;
    }
    
}
