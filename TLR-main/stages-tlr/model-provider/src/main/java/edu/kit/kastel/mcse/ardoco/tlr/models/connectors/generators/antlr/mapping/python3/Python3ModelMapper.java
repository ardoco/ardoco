package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ModuleElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public class Python3ModelMapper extends ModelMapper {
    private static final ProgrammingLanguage LANGUAGE = ProgrammingLanguage.PYTHON3;
    private final Python3ElementManager elementManager;



    public Python3ModelMapper(CodeItemRepository codeItemRepository, Python3ElementManager elementManager) {
        super(codeItemRepository, LANGUAGE);
        this.elementManager = elementManager;
    }

    @Override
    protected List<Parent> getRootParents() {
        return elementManager.getRootParents();
    }

    @Override
    protected CodeItem buildSubtree(Parent parent) {
        return buildCodePackage(parent);
    }

    @Override
    protected List<BasicElement> getElementsWithParent(Parent parent) {
        return elementManager.getElementsWithParent(parent);
    }

    @Override
    protected CodeItem buildCodeItem(BasicElement element) {
        Parent comparable;
        if (elementManager.isPackageElement(element)) {
            comparable = new Parent(element.getName(), element.getPath(), BasicType.PACKAGE);
            return buildCodePackage(comparable);
        } else if (elementManager.isModuleElement(element)) {
            comparable = new Parent(element.getName(), element.getPath(), BasicType.MODULE);
            return buildCodeAssembly(comparable);
        } else if (elementManager.isClassElement(element)) {
            comparable = new Parent(element.getName(), element.getPath(), BasicType.CLASS);
            return buildClassUnit(comparable);
        } else if (elementManager.isControlElement(element)) {
            comparable = new Parent(element.getName(), element.getPath(), BasicType.CONTROL);
            return buildControlElement(comparable);
        } else {
            return null;
        }
    }

    private CodePackage buildCodePackage(Parent parent) {
        PackageElement packageElement = elementManager.getPackage(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        CodePackage codePackage = new CodePackage(codeItemRepository, packageElement.getShortName(), content);
        codePackage.setComment(packageElement.getComment());
        return codePackage;
    }

    private CodeAssembly buildCodeAssembly(Parent parent) {
        Python3ModuleElement module = elementManager.getModule(parent);
        SortedSet<CodeItem> content = buildContent(parent);
        CodeAssembly codeAssembly = new CodeAssembly(codeItemRepository, module.getName(), content);
        codeAssembly.setComment(module.getComment());
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
        BasicElement controlElement = elementManager.getControl(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        ControlElement control = new ControlElement(codeItemRepository, controlElement.getName());
        control.setComment(controlElement.getComment());
        return control;
    }

}