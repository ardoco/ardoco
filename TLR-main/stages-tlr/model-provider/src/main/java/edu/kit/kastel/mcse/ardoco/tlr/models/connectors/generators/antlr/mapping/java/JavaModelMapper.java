package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.InterfaceUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaElementManager;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public class JavaModelMapper extends ModelMapper {
    private static final ProgrammingLanguage LANGUAGE = ProgrammingLanguage.JAVA;
    private final JavaElementManager elementManager;

    public JavaModelMapper(CodeItemRepository codeItemRepository, JavaElementManager elementManager) {
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
        } else if (elementManager.isCompilationUnitElement(element)) {
            comparable = new Parent(element.getName(), element.getPath(), BasicType.MODULE);
            return buildCodeCompilationUnit(comparable);
        } else if (elementManager.isClassElement(element)) {
            comparable = new Parent(element.getName(), element.getPath(), BasicType.CLASS);
            return buildClassUnit(comparable);
        } else if (elementManager.isInterfaceElement(element)) {
            comparable = new Parent(element.getName(), element.getPath(), BasicType.INTERFACE);
            return buildInterfaceUnit(comparable);
        } else if (elementManager.isFunctionElement(element)) {
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

    private CodeCompilationUnit buildCodeCompilationUnit(Parent parent) {
        BasicElement compilationUnit = elementManager.getCompilationUnitElement(parent);
        List<String> pathElements = Arrays.asList(compilationUnit.getPath().split("/"));
        SortedSet<CodeItem> content = buildContent(parent);
        
        PackageElement pack = elementManager.getPackage(compilationUnit.getParent());
        CodeCompilationUnit codeCompilationUnit = new CodeCompilationUnit(codeItemRepository, compilationUnit.getName(), content, pathElements, pack.getName(), language);
        codeCompilationUnit.setComment(compilationUnit.getComment());
        return codeCompilationUnit;
    }

    private ClassUnit buildClassUnit(Parent parent) {
        JavaClassElement classElement = elementManager.getClass(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        ClassUnit classUnit = new ClassUnit(codeItemRepository, classElement.getName(), content);
        classUnit.setComment(classElement.getComment());
        return classUnit;
    }

    private InterfaceUnit buildInterfaceUnit(Parent parent) {
        InterfaceElement interfaceElement = elementManager.getInterface(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        InterfaceUnit interfaceUnit = new InterfaceUnit(codeItemRepository, interfaceElement.getName(), content);
        interfaceUnit.setComment(interfaceElement.getComment());
        return interfaceUnit;
    }

    private ControlElement buildControlElement(Parent parent) {
        BasicElement function = elementManager.getFunction(parent);
        SortedSet<CodeItem> content = buildContent(parent);

        ControlElement controlElement = new ControlElement(codeItemRepository, function.getName());
        controlElement.setComment(function.getComment());
        return controlElement;
    }
}
