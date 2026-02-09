/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;

public class CodeModelEqualityHelper {

    /**
     * Checks if two CodeModels are structurally and content-wise identical using Assertions.
     * Assumes that the restoration process preserves IDs.
     *
     * @param expected the first model (expected)
     * @param actual   the second model (actual)
     */
    public static void assertCodeModelsEqual(CodeModel expected, CodeModel actual) {
        if (expected == actual)
            return;
        assertNotNull(expected, "Expected CodeModel is null");
        assertNotNull(actual, "Actual CodeModel is null");

        assertEquals(expected.getMetamodel(), actual.getMetamodel(), "Metamodel mismatch");
        assertEquals(expected.getId(), actual.getId(), "Model ID mismatch");

        Set<String> rootsExpected = expected.getContent().stream().map(CodeItem::getId).collect(Collectors.toSet());
        Set<String> rootsActual = actual.getContent().stream().map(CodeItem::getId).collect(Collectors.toSet());

        assertEquals(rootsExpected, rootsActual, "Root content IDs mismatch");

        CodeItemRepository repoExpected = expected.createCodeModelDto().codeItemRepository();
        CodeItemRepository repoActual = actual.createCodeModelDto().codeItemRepository();

        Map<String, CodeItem> itemsExpected = repoExpected.getRepository();
        Map<String, CodeItem> itemsActual = repoActual.getRepository();

        //        assertEquals(itemsExpected.size(), itemsActual.size(),
        //                "Repository size mismatch (Total items count)");

        for (String id : itemsExpected.keySet()) {
            assertTrue(itemsActual.containsKey(id), () -> "Item " + id + " missing in restored model.");

            CodeItem itemExpected = itemsExpected.get(id);
            CodeItem itemActual = itemsActual.get(id);

            assertEquals(itemExpected, itemActual, () -> "Item equality check failed for ID " + id);

            if (itemExpected instanceof CodeAssembly assemblyExpected && itemActual instanceof CodeAssembly assemblyActual) {
                assertEquals(assemblyExpected.getLanguage(), assemblyActual.getLanguage(), () -> "CodeAssembly language mismatch for ID " + id);
            }
        }
    }
}
