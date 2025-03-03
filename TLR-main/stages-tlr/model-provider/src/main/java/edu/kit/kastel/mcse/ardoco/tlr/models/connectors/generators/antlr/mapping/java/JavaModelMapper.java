package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.InterfaceUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CompilationUnitElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.InterfaceElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.java.JavaClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public class JavaModelMapper implements ModelMapper{
    private static final ProgrammingLanguage programmingLanguage = ProgrammingLanguage.JAVA;
    private List<VariableElement> variables;
    private List<ControlElement> controls;
    private List<JavaClassElement> classes;
    private List<InterfaceElement> interfaces;
    private List<CompilationUnitElement> compilationUnits;
    private List<PackageElement> packages;
    private CodeModel codeModel;
    private final CodeItemRepository codeItemRepository;


    public JavaModelMapper(CodeItemRepository codeItemRepository, List<VariableElement> variables, List<ControlElement> controls, 
    List<JavaClassElement> classes, List<InterfaceElement> interfaces, List<CompilationUnitElement> compilationUnits, List<PackageElement> packages,
    List<CommentElement> comments) {
        this.codeItemRepository = codeItemRepository;

        JavaCommentMapper javaCommentMapper = new JavaCommentMapper(variables, controls, classes, interfaces, comments);
        javaCommentMapper.mapComments();
        this.variables = javaCommentMapper.getVariables();
        this.controls = javaCommentMapper.getControls();
        this.classes = javaCommentMapper.getClasses();
        this.interfaces = javaCommentMapper.getInterfaces();
        
        this.compilationUnits = compilationUnits;
        this.packages = packages;
    }
    

    public CodeModel getCodeModel() {
        return codeModel;
    }

    public void mapToCodeModel() {

        SortedSet<CodeItem> content = new TreeSet<>();
        List<PackageElement> rootPackages = new ArrayList<>();

        for (PackageElement packageElement : packages) {
            if (packageNotAdded(packageElement, rootPackages)) {
                content.add(buildCodePackage(packageElement));
                rootPackages.add(packageElement);
            }
        }
        codeModel = new CodeModel(codeItemRepository, content);

    }

    private CodePackage buildCodePackage(PackageElement packageElement) {
        List<CompilationUnitElement> compilationUnitsOfPackage = getAllCompilationUnitElementsWith(packageElement);
        SortedSet<CodeItem> content = new TreeSet<>();
        
        for (CompilationUnitElement compilationUnitElement : compilationUnitsOfPackage) {
            content.add(buildCodeCompilationUnit(compilationUnitElement));
        }

        for (PackageElement innerPackage : packages) {
            if (innerPackage.extendsPackage(packageElement)) {
                innerPackage.updateShortName(innerPackage.getShortName().substring(packageElement.getName().length() + 1));
                content.add(buildCodePackage(innerPackage));
            }
        }
        return new CodePackage(codeItemRepository, packageElement.getShortName(), content);
    }

    private CodeCompilationUnit buildCodeCompilationUnit(CompilationUnitElement compilationUnit) {
        String name = compilationUnit.getName();
        String path = compilationUnit.getPath();
        Parent comparable = new Parent(name, path, BasicType.COMPILATIONUNIT);
        List<JavaClassElement> classesOfCompilationUnit = getAllClassesWith(comparable);
        List<InterfaceElement> interfacesOfCompilationUnit = getAllInterfacesWith(comparable);
        SortedSet<CodeItem> content = new TreeSet<>();

        for (JavaClassElement classElement : classesOfCompilationUnit) {
            content.add(buildClassUnit(classElement));
        }
        for (InterfaceElement interfaceElement : interfacesOfCompilationUnit) {
            content.add(buildInterfaceUnit(interfaceElement));
        }
        List<String> pathElements = Arrays.asList(compilationUnit.getPath().split("/"));
        return new CodeCompilationUnit(codeItemRepository, name, content, pathElements, compilationUnit.getPackage().getName(), programmingLanguage);
    }


    private ClassUnit buildClassUnit(JavaClassElement classElement) {
        String name = classElement.getName();
        String path = classElement.getPath();
        String comment = classElement.getComment();
        Parent comparable = new Parent(name, path, BasicType.CLASS);
        List<ControlElement> controlsOfClass = getAllControlsWith(comparable);
        List<JavaClassElement> innerClasses = getAllClassesWith(comparable);
        SortedSet<CodeItem> content = new TreeSet<>();

        // variables not implemented yet
        for (ControlElement control : controlsOfClass) {
            content.add(buildControlElement(control));
        }
        for (JavaClassElement innerClass : innerClasses) {
            content.add(buildClassUnit(innerClass));
        }
        ClassUnit classUnit = new ClassUnit(codeItemRepository, name, content);
        if (comment != null) {
            classUnit.setComment(comment);
        }
        return classUnit;      
    }

    private InterfaceUnit buildInterfaceUnit(InterfaceElement interfaceElement) {
        String name = interfaceElement.getName();
        String path = interfaceElement.getPath();
        String comment = interfaceElement.getComment();
        Parent comparable = new Parent(name, path, BasicType.INTERFACE);
        List<ControlElement> controlsOfInterface = getAllControlsWith(comparable);
        SortedSet<CodeItem> content = new TreeSet<>();

        for (ControlElement control : controlsOfInterface) {
            content.add(buildControlElement(control));
        }
        InterfaceUnit interfaceUnit = new InterfaceUnit(codeItemRepository, name, content);
        if (comment != null) {
            interfaceUnit.setComment(comment);
        }
        return interfaceUnit;
    }

    private edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement buildControlElement(ControlElement controlElement) {
        String name = controlElement.getName();
        String path = controlElement.getPath();
        String comment = controlElement.getComment();
        Parent comparable = new Parent(name, path, BasicType.CONTROL);

        List<VariableElement> contentOfControl = new ArrayList<>();
        for (VariableElement variable : variables) {
            if (variable.getParent().equals(comparable)) {
                contentOfControl.add(variable);
                // variables not implemented yet
            }
        }
        edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement control = new edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement(codeItemRepository, name);
        if (comment != null) {
            control.setComment(comment);
        }
        return control;
    }

    private List<ControlElement> getAllControlsWith(Parent parent) {
        List<ControlElement> controlsWithParent = new ArrayList<>();
        for (ControlElement control : controls) {
            if (control.getParent().equals(parent)) {
                controlsWithParent.add(control);
            }
        }
        return controlsWithParent;
    }

    private List<JavaClassElement> getAllClassesWith(Parent parent) {
        List<JavaClassElement> classesWithParent = new ArrayList<>();
        for (JavaClassElement classElement : classes) {
            if (classElement.getParent().equals(parent)) {
                classesWithParent.add(classElement);
            }
        }
        return classesWithParent;
    }

    private List<InterfaceElement> getAllInterfacesWith(Parent parent) {
        List<InterfaceElement> interfacesWithParent = new ArrayList<>();
        for (InterfaceElement interfaceElement : interfaces) {
            if (interfaceElement.getParent().equals(parent)) {
                interfacesWithParent.add(interfaceElement);
            }
        }
        return interfacesWithParent;
    }

    private List<CompilationUnitElement> getAllCompilationUnitElementsWith(PackageElement packageElement) {
        List<CompilationUnitElement> compilationUnitElementsOfPackage = new ArrayList<>();
        for (CompilationUnitElement compilationUnitElement : compilationUnits) {
            if (compilationUnitElement.getPackage().equals(packageElement)) {
                compilationUnitElementsOfPackage.add(compilationUnitElement);
            }
        }
        return compilationUnitElementsOfPackage;
    }

    private boolean packageNotAdded(PackageElement packageElement, List<PackageElement> addedPackages) {
        for (PackageElement addedPackage : addedPackages) {
            if (packageElement.extendsPackage(addedPackage)) {
                return false;
            }
        }
        return true;
    }
}
