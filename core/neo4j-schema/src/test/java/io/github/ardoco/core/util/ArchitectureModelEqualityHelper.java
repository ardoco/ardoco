package io.github.ardoco.core.util;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ArchitectureModelEqualityHelper {

    /**
     * Deep compares two ArchitectureModels using Assertions.
     *
     * @param expected The original model
     * @param actual   The restored model
     */
    public static void assertArchitectureModelsEqual(ArchitectureModel expected, ArchitectureModel actual) {
        if (expected == actual) return;
        assertNotNull(expected, "Expected ArchitectureModel is null");
        assertNotNull(actual, "Actual ArchitectureModel is null");

        assertEquals(expected.getMetamodel(), actual.getMetamodel(), "Metamodel mismatch");
        assertEquals(expected.getId(), actual.getId(), "Model ID mismatch");

        Map<String, ArchitectureItem> itemsExpected = mapContent(expected.getContent());
        Map<String, ArchitectureItem> itemsActual = mapContent(actual.getContent());

        assertEquals(itemsExpected.size(), itemsActual.size(),
                () -> "Content size mismatch. Expected keys: " + itemsExpected.keySet() + ", Actual keys: " + itemsActual.keySet());

        for (String id : itemsActual.keySet()) {
            assertTrue(itemsActual.containsKey(id), () -> "Restored model missing item with ID: " + id);

            ArchitectureItem itemExpected = itemsExpected.get(id);
            ArchitectureItem itemActual = itemsActual.get(id);

            assertItemsEqual(itemExpected, itemActual);
        }
    }

    private static Map<String, ArchitectureItem> mapContent(List<? extends ArchitectureItem> content) {
        return content.stream().collect(Collectors.toMap(ArchitectureItem::getId, item -> item));
    }

    private static void assertItemsEqual(ArchitectureItem expected, ArchitectureItem actual) {
        assertEquals(expected.getClass(), actual.getClass(),
                () -> "Class mismatch for ID " + expected.getId());

        assertEquals(expected.getName(), actual.getName(),
                () -> "Name mismatch for ID " + expected.getId());

        if (expected instanceof ArchitectureComponent compExpected && actual instanceof ArchitectureComponent compActual) {
            assertComponentsEqual(compExpected, compActual);
        } else if (expected instanceof ArchitectureInterface ifaceExpected && actual instanceof ArchitectureInterface ifaceActual) {
            assertInterfacesEqual(ifaceExpected, ifaceActual);
        }
    }

    private static void assertComponentsEqual(ArchitectureComponent expected, ArchitectureComponent actual) {
        String context = "Component: " + expected.getName() + " (" + expected.getId() + ")";

        assertEquals(expected.getType(), actual.getType(), context + " - Type mismatch");

        Set<String> subExpected = expected.getSubcomponents().stream().map(ArchitectureComponent::getId).collect(Collectors.toSet());
        Set<String> subActual = actual.getSubcomponents().stream().map(ArchitectureComponent::getId).collect(Collectors.toSet());
        assertEquals(subExpected, subActual, context + " - Subcomponents mismatch");

        Set<String> provExpected = expected.getProvidedInterfaces().stream().map(ArchitectureInterface::getId).collect(Collectors.toSet());
        Set<String> provActual = actual.getProvidedInterfaces().stream().map(ArchitectureInterface::getId).collect(Collectors.toSet());
        assertEquals(provExpected, provActual, context + " - Provided Interfaces mismatch");

        Set<String> reqExpected = expected.getRequiredInterfaces().stream().map(ArchitectureInterface::getId).collect(Collectors.toSet());
        Set<String> reqActual = actual.getRequiredInterfaces().stream().map(ArchitectureInterface::getId).collect(Collectors.toSet());
        assertEquals(reqExpected, reqActual, context + " - Required Interfaces mismatch");
    }

    private static void assertInterfacesEqual(ArchitectureInterface expected, ArchitectureInterface actual) {
        String context = "Interface: " + expected.getName() + " (" + expected.getId() + ")";

        Set<String> sigsExpected = expected.getMethodSignatures().stream().map(ArchitectureMethod::getName).collect(Collectors.toSet());
        Set<String> sigsActual = actual.getMethodSignatures().stream().map(ArchitectureMethod::getName).collect(Collectors.toSet());

        assertEquals(sigsExpected, sigsActual, context + " - Method signatures mismatch");
    }
}
