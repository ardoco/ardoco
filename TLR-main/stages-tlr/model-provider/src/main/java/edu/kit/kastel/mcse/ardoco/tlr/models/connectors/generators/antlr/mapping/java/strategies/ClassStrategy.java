package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.strategies;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.java.JavaElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java.JavaCodeItemBuilder;

public class ClassStrategy extends AbstractJavaCodeItemStrategy {

    public ClassStrategy(CodeItemRepository codeItemRepository, JavaCodeItemBuilder javaCodeItemBuilder,
            JavaElementManager elementManager) {
        super(codeItemRepository, javaCodeItemBuilder, elementManager);
    }

    @Override
    public boolean supports(Element element) {
        return this.elementManager.isClassElement(element);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.CLASS);
        return buildClassUnit(comparable);
    }

    private ClassUnit buildClassUnit(ElementIdentifier parent) {
        JavaClassElement classElement = elementManager.getClass(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        ClassUnit classUnit = new ClassUnit(codeItemRepository, classElement.getName(), content);
        classUnit.setComment(classElement.getComment());
        return classUnit;
    }

}
