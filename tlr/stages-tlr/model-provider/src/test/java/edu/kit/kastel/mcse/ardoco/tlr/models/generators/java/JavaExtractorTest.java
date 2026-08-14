/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.models.generators.java;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.ControlElement;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.java.JavaExtractor;

class JavaExtractorTest {
    private static final Logger logger = LoggerFactory.getLogger(JavaExtractorTest.class);

    @Test
    void extractorTest() {
        var extractor = new JavaExtractor(new CodeItemRepository(), "src/test/resources/interface", Metamodel.CODE_WITH_COMPILATION_UNITS);
        CodeModel model = extractor.extractModel();
        Assertions.assertNotNull(model);
        for (Entity codePackage : model.getAllPackages()) {
            Assertions.assertNotNull(codePackage);
            logger.info("Package: {}", codePackage);
        }

        Assertions.assertEquals(8, model.getEndpoints().size());

        List<CodeCompilationUnit> allCompilationUnits = model.getAllPackages().stream().flatMap(p -> p.getCompilationUnits().stream()).toList();
        CodeCompilationUnit aClass = allCompilationUnits.stream().filter(u -> "AClass".equals(u.getName())).findFirst().orElseThrow();
        Assertions.assertTrue(aClass.getImportedModuleNames().contains("edu.zwei.OtherInterface"));

        CodeCompilationUnit callsUnit = allCompilationUnits.stream().filter(u -> "AClassWithCalls".equals(u.getName())).findFirst().orElseThrow();
        SortedSet<ControlElement> allMethods = new TreeSet<>();
        for (var dt : callsUnit.getAllDataTypes()) {
            allMethods.addAll(dt.getDeclaredMethods());
        }

        ControlElement caller = allMethods.stream().filter(m -> "caller".equals(m.getName())).findFirst().orElseThrow();
        Assertions.assertTrue(caller.getCalleeNames().contains("helper"));
        Assertions.assertTrue(caller.getCalleeNames().contains("TestClass"));
        Assertions.assertTrue(caller.getCalleeNames().contains("method"));

        Assertions.assertTrue(caller.getCalleeNames().contains("ArrayList"));
        Assertions.assertFalse(caller.getCalleeNames().stream().anyMatch(name -> name.contains(".") || name.contains("<")));

        ControlElement helper = allMethods.stream().filter(m -> "helper".equals(m.getName())).findFirst().orElseThrow();
        Assertions.assertTrue(helper.getCalleeNames().isEmpty());
    }
}
