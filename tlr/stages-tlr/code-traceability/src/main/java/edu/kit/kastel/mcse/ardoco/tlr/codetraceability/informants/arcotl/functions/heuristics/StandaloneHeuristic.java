/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.StandaloneHeuristicNode;

/**
 * A heuristic that does not depend on any existing computation node's result.
 */
public abstract class StandaloneHeuristic extends Heuristic {

    private @Nullable CodeModel codeModel;

    public StandaloneHeuristicNode getNode() {
        return new StandaloneHeuristicNode(this);
    }

    protected CodeModel getCodeModel() {
        return Objects.requireNonNull(codeModel, "Confidences have not been calculated yet");
    }

    public NodeResult calculateConfidences(ArchitectureModel archModel, CodeModel codeModel) {
        this.codeModel = codeModel;
        return getNodeResult(archModel, codeModel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StandaloneHeuristic that))
            return false;
        if (!super.equals(o))
            return false;

        return Objects.equals(codeModel, that.codeModel);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (codeModel != null ? codeModel.hashCode() : 0);
        return result;
    }
}
