package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.cpp;

import java.util.List;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.cpp.CppElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public class CppModelMapper extends ModelMapper {
    private static final ProgrammingLanguage LANGUAGE = ProgrammingLanguage.CPP;
    private final CppElementManager elementManager;

    public CppModelMapper(CodeItemRepository codeItemRepository, CppElementManager elementManager) {
        super(codeItemRepository, LANGUAGE);
        this.elementManager = elementManager;
    }

    @Override
    protected List<Parent> getRootParents() {
        return elementManager.getRootParents();
    }

    @Override
    protected CodeItem buildSubtree(Parent parent) {
        String name = parent.getPath().substring(parent.getPath().lastIndexOf("/") + 1);
        SortedSet<CodeItem> content = buildContent(parent);
        CodeAssembly codeAssembly = new CodeAssembly(codeItemRepository, name, content);
        return codeAssembly;
    }

    @Override
    protected List<BasicElement> getElementsWithParent(Parent parent) {
        return elementManager.getElementsWithParent(parent);
    }

    @Override
    protected CodeItem buildCodeItem(BasicElement element) {
        Parent comparable;
        if (elementManager.isNamespaceElement(element)) {
            comparable = new Parent(element.getName(), element.getPath(), BasicType.NAMESPACE);
            return buildCodeAssembly(comparable);
        } else if (elementManager.isClassElement(element)) {
            comparable = new Parent(element.getName(), element.getPath(), BasicType.CLASS);
            return buildClassUnit(comparable);
        } else if (elementManager.isFunctionElement(element)) {
            comparable = new Parent(element.getName(), element.getPath(), BasicType.CONTROL);
            return buildControlElement(comparable);
        } else {
            return null;
        }
    }

    private CodeAssembly buildCodeAssembly(Parent parent) {
        BasicElement namespace = elementManager.getNamespace(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        CodeAssembly codeAssembly = new CodeAssembly(codeItemRepository, namespace.getName(), content);
        codeAssembly.setComment(namespace.getComment());
        return codeAssembly;
    }

    private ClassUnit buildClassUnit(Parent parent) {
        ClassElement classElement = elementManager.getClass(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        ClassUnit classUnit = new ClassUnit(codeItemRepository, classElement.getName(), content);
        classUnit.setComment(classElement.getComment());
        return classUnit;
    }

    private ControlElement buildControlElement(Parent parent) {
        BasicElement function = elementManager.getFunction(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        ControlElement controlElement = new ControlElement(codeItemRepository, function.getName());
        controlElement.setComment(function.getComment());
        return controlElement;
    }
}
