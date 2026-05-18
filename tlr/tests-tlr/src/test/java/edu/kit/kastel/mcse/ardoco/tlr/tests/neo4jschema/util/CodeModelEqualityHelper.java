/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;

public class CodeModelEqualityHelper {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CodeModelEqualityHelper.class);

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

        Set<String> expectedIds = repoExpected.getRepository().keySet();
        Set<String> actualIds = repoActual.getRepository().keySet();

        Set<String> missingIds = new HashSet<>(expectedIds);
        missingIds.removeAll(actualIds);

        System.out.println("Total missing items: " + missingIds.size());
        //        for (String id : missingIds) {
        //            CodeItem missingItem = repoExpected.getCodeItem(id);
        ////            System.out.println("Missing: " + id + " | Type: " + missingItem.getClass().getSimpleName() + " | Name: " + missingItem.getName());
        //        }

        int hasChildrenCount = 0;
        int isContainedByOtherCount = 0;
        for (String id : missingIds) {
            CodeItem item = repoExpected.getCodeItem(id);

            // Check 1: Does it have children?
            boolean hasChildren = !item.getContent().isEmpty();

            // Check 2: Does it have a parent?
            // You'll need to check the repository for items that contain this ID
            boolean isContainedByOther = repoExpected.getRepository()
                    .values()
                    .stream()
                    .anyMatch(other -> other.getContent().stream().anyMatch(child -> child.getId().equals(id)));

            if (!hasChildren) {
                System.out.println("Thesis Failed: Missing item " + id + " | Type: " + item.getClass().getSimpleName() + " | Name: " + item
                        .getName() + " actually has children!");
                hasChildrenCount++;
            }

            if (isContainedByOther) {
                System.out.println("Thesis Failed: Missing item " + id + " | Type: " + item.getClass().getSimpleName() + " | Name: " + item
                        .getName() + " is a child of another item!");
                isContainedByOtherCount++;
            }
        }

        double reachability = (double) actualIds.size() / expectedIds.size() * 100;
        logger.info("Model reachability/retention: {}%", String.format("%.2f", reachability));
        logger.info("Missing items with children: {}", hasChildrenCount);
        logger.info("Missing items that are children of other items: {}", isContainedByOtherCount);

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
