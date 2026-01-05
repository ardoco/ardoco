package io.github.ardoco.core.util;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeAssembly;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItemRepository;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CodeModelEqualityHelper {

    /**
     * Checks if two CodeModels are structurally and content-wise identical.
     * Assumes that the restoration process preserves IDs (which your architecture does).
     *
     * @param modelA the first model
     * @param modelB the second model
     * @return true if equal, false otherwise
     */
    public static boolean areCodeModelsEqual(CodeModel modelA, CodeModel modelB) {
        if (modelA == modelB) return true;
        if (modelA == null || modelB == null) return false;

        // 1. Check Metamodel & ID
        if (modelA.getMetamodel() != modelB.getMetamodel()) {
            System.err.println("Metamodel mismatch: " + modelA.getMetamodel() + " vs " + modelB.getMetamodel());
            return false;
        }
        if (!Objects.equals(modelA.getId(), modelB.getId())) {
            System.err.println("Model ID mismatch: " + modelA.getId() + " vs " + modelB.getId());
            return false;
        }

        // 2. Check Root Content (Order independent check of IDs)
        Set<String> rootsA = modelA.getContent().stream().map(CodeItem::getId).collect(Collectors.toSet());
        Set<String> rootsB = modelB.getContent().stream().map(CodeItem::getId).collect(Collectors.toSet());

        if (!rootsA.equals(rootsB)) {
            System.err.println("Root content mismatch. A=" + rootsA + ", B=" + rootsB);
            return false;
        }

        // 3. Deep Compare All Items in Repository
        // We use the DTO to access the internal initialized repositories
        CodeItemRepository repoA = modelA.createCodeModelDto().codeItemRepository();
        CodeItemRepository repoB = modelB.createCodeModelDto().codeItemRepository();

        Map<String, CodeItem> itemsA = repoA.getRepository();
        Map<String, CodeItem> itemsB = repoB.getRepository();

        if (itemsA.size() != itemsB.size()) {
            System.err.println("Repository size mismatch: " + itemsA.size() + " vs " + itemsB.size());
            return false;
        }

        for (String id : itemsA.keySet()) {
            if (!itemsB.containsKey(id)) {
                System.err.println("Item " + id + " missing in restored model.");
                return false;
            }

            CodeItem itemA = itemsA.get(id);
            CodeItem itemB = itemsB.get(id);

            // 3a. Standard Equals Check (Covered by CodeItem subclasses)
            if (!itemA.equals(itemB)) {
                System.err.println("Item inequality for ID " + id + ": " + itemA + " vs " + itemB);
                return false;
            }

            // 3b. Manual Check for fields missing in equals()
            // CodeAssembly does not override equals(), so it ignores 'language'. We check it manually.
            if (itemA instanceof CodeAssembly assemblyA && itemB instanceof CodeAssembly assemblyB) {
                if (!Objects.equals(assemblyA.getLanguage(), assemblyB.getLanguage())) {
                    System.err.println("CodeAssembly language mismatch for " + id);
                    return false;
                }
            }

            // Note: CodeCompilationUnit, ClassUnit, InterfaceUnit, Datatype override equals() correctly.
        }

        return true;
    }
}
