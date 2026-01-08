package io.github.ardoco.core.util;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;

import java.util.*;
import java.util.stream.Collectors;

public class ArchitectureModelEqualityHelper {

    /**
     * Deep compares two ArchitectureModels.
     *
     * @param modelA The original model
     * @param modelB The restored model
     * @return true if structurally identical
     */
    public static boolean areArchitectureModelsEqual(ArchitectureModel modelA, ArchitectureModel modelB) {
        if (modelA == modelB) return true;
        if (modelA == null || modelB == null) return false;

        // 1. Check Meta-info
        if (modelA.getMetamodel() != modelB.getMetamodel()) {
            System.err.println("Metamodel mismatch: " + modelA.getMetamodel() + " vs " + modelB.getMetamodel());
            return false;
        }
        if (!Objects.equals(modelA.getId(), modelB.getId())) {
            System.err.println("Model ID mismatch: " + modelA.getId() + " vs " + modelB.getId());
            return false;
        }

        // 2. Map Content by ID for comparison
        Map<String, ArchitectureItem> itemsA = mapContent(modelA.getContent());
        Map<String, ArchitectureItem> itemsB = mapContent(modelB.getContent());

        if (itemsA.size() != itemsB.size()) {
            System.err.println("Content size mismatch: " + itemsA.size() + " vs " + itemsB.size());
            return false;
        }

        // 3. Compare each item
        for (String id : itemsA.keySet()) {
            if (!itemsB.containsKey(id)) {
                System.err.println("Restored model missing item: " + id);
                return false;
            }

            ArchitectureItem itemA = itemsA.get(id);
            ArchitectureItem itemB = itemsB.get(id);

            if (!areItemsEqual(itemA, itemB)) {
                System.err.println("Item mismatch for ID " + id + " (" + itemA.getName() + ")");
                return false;
            }
        }

        return true;
    }

    private static Map<String, ArchitectureItem> mapContent(List<? extends ArchitectureItem> content) {
        return content.stream().collect(Collectors.toMap(ArchitectureItem::getId, item -> item));
    }

    private static boolean areItemsEqual(ArchitectureItem a, ArchitectureItem b) {
        if (a.getClass() != b.getClass()) {
            System.err.println("Class mismatch: " + a.getClass() + " vs " + b.getClass());
            return false;
        }
        if (!Objects.equals(a.getName(), b.getName())) {
            System.err.println("Name mismatch: " + a.getName() + " vs " + b.getName());
            return false;
        }

        if (a instanceof ArchitectureComponent compA && b instanceof ArchitectureComponent compB) {
            return areComponentsEqual(compA, compB);
        } else if (a instanceof ArchitectureInterface ifaceA && b instanceof ArchitectureInterface ifaceB) {
            return areInterfacesEqual(ifaceA, ifaceB);
        }

        return true;
    }

    private static boolean areComponentsEqual(ArchitectureComponent a, ArchitectureComponent b) {
        // Compare Type
        if (!Objects.equals(a.getType(), b.getType())) return false;

        // Compare Subcomponents (by ID)
        Set<String> subA = a.getSubcomponents().stream().map(ArchitectureComponent::getId).collect(Collectors.toSet());
        Set<String> subB = b.getSubcomponents().stream().map(ArchitectureComponent::getId).collect(Collectors.toSet());
        if (!subA.equals(subB)) {
            System.err.println("Subcomponents mismatch for " + a.getName());
            return false;
        }

        // Compare Provided Interfaces (by ID)
        Set<String> provA = a.getProvidedInterfaces().stream().map(ArchitectureInterface::getId).collect(Collectors.toSet());
        Set<String> provB = b.getProvidedInterfaces().stream().map(ArchitectureInterface::getId).collect(Collectors.toSet());
        if (!provA.equals(provB)) {
            System.err.println("Provided Interfaces mismatch for " + a.getName());
            return false;
        }

        // Compare Required Interfaces (by ID)
        Set<String> reqA = a.getRequiredInterfaces().stream().map(ArchitectureInterface::getId).collect(Collectors.toSet());
        Set<String> reqB = b.getRequiredInterfaces().stream().map(ArchitectureInterface::getId).collect(Collectors.toSet());
        if (!reqA.equals(reqB)) {
            System.err.println("Required Interfaces mismatch for " + a.getName());
            return false;
        }

        return true;
    }

    private static boolean areInterfacesEqual(ArchitectureInterface a, ArchitectureInterface b) {
        // Compare Signatures
        Set<String> sigsA = a.getMethodSignatures().stream().map(ArchitectureMethod::getName).collect(Collectors.toSet());
        Set<String> sigsB = b.getMethodSignatures().stream().map(ArchitectureMethod::getName).collect(Collectors.toSet());

        if (!sigsA.equals(sigsB)) {
            System.err.println("Method signatures mismatch for interface " + a.getName());
            return false;
        }
        return true;
    }
}
