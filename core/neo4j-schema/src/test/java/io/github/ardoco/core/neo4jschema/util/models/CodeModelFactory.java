package io.github.ardoco.core.neo4jschema.util.models;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnitsAndPackages;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.InterfaceUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ProgrammingLanguage;

public class CodeModelFactory {

    public static CodeModelWithCompilationUnitsAndPackages createCodeModel() {
        CodeItemRepository repo = new CodeItemRepository();
        String modelId = "complex_test_code_model";

        InterfaceUnit authInterface = new InterfaceUnit("id_iface_auth", repo, "IAuthenticator", new TreeSet<>());

        ClassUnit ldapClass = new ClassUnit("id_cls_ldap", repo, "LDAPAuthenticator", new TreeSet<>());
        ldapClass.setImplementedTypes(new TreeSet<>(List.of(authInterface)));

        CodeCompilationUnit interfaceCU = new CodeCompilationUnit("id_cu_iface", repo, "IAuthenticator", new TreeSet<>(), List.of("edu", "kit", "api"), "java",
                ProgrammingLanguage.JAVA);
        interfaceCU.addContent(authInterface);
        authInterface.setCompilationUnit(interfaceCU);
        CodeCompilationUnit classCU = new CodeCompilationUnit("id_cu_cls", repo, "LDAPAuthenticator", new TreeSet<>(), List.of("edu", "kit", "impl"), "java",
                ProgrammingLanguage.JAVA);
        classCU.addContent(ldapClass);
        ldapClass.setCompilationUnit(classCU);

        CodePackage apiPkg = new CodePackage("id_pkg_api", repo, "edu.kit.api");
        apiPkg.addContent(interfaceCU);
        interfaceCU.setParent(apiPkg);
        CodePackage implPkg = new CodePackage("id_pkg_impl", repo, "edu.kit.impl");
        implPkg.addContent(classCU);
        classCU.setParent(implPkg);

        repo.init();
        SortedSet<CodeItem> roots = new TreeSet<>(List.of(apiPkg, implPkg));
        return new CodeModelWithCompilationUnitsAndPackages(modelId, repo, roots);
    }

    public static CodeModelWithCompilationUnitsAndPackages createEmptyCodeModel() {
        CodeItemRepository repository = new CodeItemRepository();
        String modelId = "empty_code_model";
        repository.init();
        return new CodeModelWithCompilationUnitsAndPackages(modelId, repository, new TreeSet<>());
    }

    public static CodeModelWithCompilationUnitsAndPackages createSimpleCodeModel() {
        String modelId = "test_code_model";
        return createSimpleCodeModel(modelId);

    }

    public static CodeModelWithCompilationUnitsAndPackages createSimpleCodeModel(String modelId) {
        CodeItemRepository repository = new CodeItemRepository();

        CodePackage codePackage = new CodePackage("id_pkg_auth", repository, "edu.kit.auth");
        CodeCompilationUnit codeCompilationUnit = new CodeCompilationUnit("id_cu_auth", repository, "AuthService", new TreeSet<>(),
                List.of("edu", "kit", "auth"), "java", ProgrammingLanguage.JAVA);
        ClassUnit clazz = new ClassUnit("id_cls_auth", repository, "AuthService", new TreeSet<>());

        codePackage.addContent(codeCompilationUnit);
        codeCompilationUnit.setParent(codePackage);

        codeCompilationUnit.addContent(clazz);
        clazz.setCompilationUnit(codeCompilationUnit);

        repository.init();
        SortedSet<CodeItem> roots = new TreeSet<>(List.of(codePackage));
        return new CodeModelWithCompilationUnitsAndPackages(modelId, repository, roots);
    }
}
