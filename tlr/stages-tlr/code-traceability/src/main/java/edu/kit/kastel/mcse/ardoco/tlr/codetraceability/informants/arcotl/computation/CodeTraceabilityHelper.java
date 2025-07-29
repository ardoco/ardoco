/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;

/**
 * Helper class for CodeTraceability computations.
 */
public class CodeTraceabilityHelper {

    /**
     * Returns the cross product of all architecture items of the specified architecture model and all code compilation units of the specified code model.
     *
     * @return all pairs of architecture items and code compilation units
     */
    public static List<Pair<ArchitectureItem, CodeCompilationUnit>> crossProductFromArchitectureItemsToCompilationUnits(ArchitectureModel archModel,
            CodeModel codeModel) {

        List<Pair<ArchitectureItem, CodeCompilationUnit>> endpointTuples = new ArrayList<>();
        for (var architectureEndpoint : archModel.getEndpoints()) {
            for (var codeEndpoint : codeModel.getEndpoints()) {
                if (codeEndpoint instanceof CodeCompilationUnit codeCompilationUnit) {
                    // Currently only supported for codeCompilationUnits due to Heuristics
                    endpointTuples.add(new Pair<>(architectureEndpoint, codeCompilationUnit));
                }
            }
        }

        return endpointTuples;
    }
}
