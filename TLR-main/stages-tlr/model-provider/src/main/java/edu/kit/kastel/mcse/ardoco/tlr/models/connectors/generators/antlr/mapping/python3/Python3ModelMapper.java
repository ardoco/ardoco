package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.python3;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ProgrammingLanguage;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.BasicType;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.CommentElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.PackageElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.Parent;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.ClassElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3ModuleElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.elements.python3.Python3VariableElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.antlr.mapping.ModelMapper;

public class Python3ModelMapper implements ModelMapper {
    private ProgrammingLanguage language = ProgrammingLanguage.PYTHON3;
    private List<Python3VariableElement> variables = new ArrayList<>();
    private List<ControlElement> controls = new ArrayList<>();
    private List<ClassElement> classes = new ArrayList<>();
    private List<Python3ModuleElement> modules = new ArrayList<>();
    private List<PackageElement> packages = new ArrayList<>();
    private CodeModel codeModel;
    private final CodeItemRepository codeItemRepository;

    public Python3ModelMapper(CodeItemRepository codeItemRepository, List<Python3VariableElement> variables,
            List<ControlElement> controls,
            List<ClassElement> classes, List<Python3ModuleElement> modules, List<PackageElement> packages,
            List<CommentElement> comments) {
        Python3CommentMapper commentMapper = new Python3CommentMapper(variables, controls, classes, comments);
        commentMapper.mapComments();
        this.variables = commentMapper.getVariables();
        this.controls = commentMapper.getControls();
        this.classes = commentMapper.getClasses();
        this.modules = modules;
        this.packages = packages;
        this.codeItemRepository = codeItemRepository;
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

    private boolean packageNotAdded(PackageElement packageElement, List<PackageElement> rootPackages) {
        for (PackageElement rootPackage : rootPackages) {
            if (packageElement.extendsPackage(rootPackage)) {
                return false;
            }
        }
        return true;
    }

    private CodePackage buildCodePackage(PackageElement packageElement) {
        List<Python3ModuleElement> modules = getAllModulesInPackage(packageElement);
        SortedSet<CodeItem> content = new TreeSet<>();

        for (Python3ModuleElement module : modules) {
            content.add(buildCodeAssembly(module));
        }

        for (PackageElement innerPackage : packages) {
            if (innerPackage.extendsPackage(packageElement)) {
                innerPackage
                        .updateShortName(innerPackage.getShortName().substring(packageElement.getName().length() + 1));
                content.add(buildCodePackage(innerPackage));
            }
        }
        return new CodePackage(codeItemRepository, packageElement.getShortName(), content);
    }

    private List<Python3ModuleElement> getAllModulesInPackage(PackageElement packageElement) {
        List<Python3ModuleElement> modulesOfPackage = new ArrayList<>();
        for (Python3ModuleElement module : this.modules) {
            if (module.getPackage().equals(packageElement)) {
                modulesOfPackage.add(module);
            }
        }
        return modulesOfPackage;
    }

    private CodeAssembly buildCodeAssembly(Python3ModuleElement module) {
        String name = module.getName();
        String path = module.getPath();
        Parent comparable = new Parent(name, path, BasicType.MODULE);
        List<ClassElement> classesOfModule = getAllClassesWith(comparable);
        List<ControlElement> controlsOfModule = getAllControlsWith(comparable);
        // Variables not yet implemented
        SortedSet<CodeItem> content = new TreeSet<>();

        for (ClassElement clazz : classesOfModule) {
            content.add(buildClassUnit(clazz));
        }

        for (ControlElement control : controlsOfModule) {
            content.add(buildControlElement(control));
        }

        return new CodeAssembly(codeItemRepository, name, content);

    }

    private List<ClassElement> getAllClassesWith(Parent parent) {
        List<ClassElement> classesOfModule = new ArrayList<>();
        for (ClassElement clazz : this.classes) {
            if (clazz.getParent().equals(parent)) {
                classesOfModule.add(clazz);
            }
        }
        return classesOfModule;
    }

    private List<ControlElement> getAllControlsWith(Parent parent) {
        List<ControlElement> controlsOfModule = new ArrayList<>();
        for (ControlElement control : this.controls) {
            if (control.getParent().equals(parent)) {
                controlsOfModule.add(control);
            }
        }
        return controlsOfModule;
    }

    private ClassUnit buildClassUnit(ClassElement classElement) {
        String name = classElement.getName();
        String path = classElement.getPath();
        String comment = classElement.getComment();
        Parent comparable = new Parent(name, path, BasicType.CLASS);
        List<ControlElement> controlsOfClass = getAllControlsWith(comparable);
        List<ClassElement> innerClasses = getAllClassesWith(comparable);
        SortedSet<CodeItem> content = new TreeSet<>();

        // variables not implemented yet
        for (ControlElement control : controlsOfClass) {
            content.add(buildControlElement(control));
        }
        for (ClassElement innerClass : innerClasses) {
            content.add(buildClassUnit(innerClass));
        }
        ClassUnit classUnit = new ClassUnit(codeItemRepository, name, content);
        if (comment != null) {
            classUnit.setComment(comment);
        }
        return classUnit;
    }

    private edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement buildControlElement(
            ControlElement control) {
        String name = control.getName();
        String path = control.getPath();
        String comment = control.getComment();
        Parent comparable = new Parent(name, path, BasicType.CONTROL);
        List<Python3VariableElement> contentOfControl = new ArrayList<>();
        for (Python3VariableElement variable : variables) {
            if (variable.getParent().equals(comparable)) {
                contentOfControl.add(variable);
                // variables not implemented yet
            }
        }
        edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement controlElement = new edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement(
                codeItemRepository, name);
        if (comment != null) {
            control.setComment(comment);
        }
        return controlElement;
    }

}