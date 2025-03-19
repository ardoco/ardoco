package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.strategies;

import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Element;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ElementIdentifier;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Type;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.management.cpp.CppElementStorageRegistry;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp.CppCodeItemBuilder;

public class ClassStrategy extends AbstractCppCodeItemStrategy {

    public ClassStrategy(CodeItemRepository codeItemRepository, CppCodeItemBuilder cppCodeItemBuilder, CppElementStorageRegistry elementManager) {
        super(codeItemRepository, cppCodeItemBuilder, elementManager);
    }

    @Override
    public CodeItem buildCodeItem(Element element) {
        ElementIdentifier comparable = new ElementIdentifier(element.getName(), element.getPath(), Type.CLASS);
        return buildClassUnit(comparable);
    }

    @Override
    public boolean supports(Element element) {
        return this.elementManager.isClassElement(element);
    }


    private CodeItem buildClassUnit(ElementIdentifier parent) {
        ClassElement classElement = this.elementManager.getClass(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        ClassUnit classUnit = new ClassUnit(this.codeItemRepository, classElement.getName(), content);
        classUnit.setComment(classElement.getComment());
        return classUnit;
    }
    
}
