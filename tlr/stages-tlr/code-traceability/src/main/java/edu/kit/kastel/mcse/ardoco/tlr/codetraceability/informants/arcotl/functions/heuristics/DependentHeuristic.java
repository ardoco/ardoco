/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.DependentHeuristicNode;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.Node;

/**
 * A heuristic that depends on exactly one computation node's result. To apply
 * this heuristic there need to already exist some calculated confidences that
 * can be used.
 */
public abstract class DependentHeuristic extends Heuristic {

    private @Nullable NodeResult nodeResult;
    private @Nullable ArchitectureModel archModel;

    public DependentHeuristicNode getNode(Node child) {
        return new DependentHeuristicNode(this, child);
    }

    protected NodeResult getNodeResult() {
        return Objects.requireNonNull(nodeResult, "Confidences have not been calculated yet");
    }

    protected Confidence getRequiredConfidence(Pair<ArchitectureItem, CodeCompilationUnit> endpointTuple) {
        return Objects.requireNonNull(getNodeResult().getConfidence(endpointTuple), "Missing confidence for endpoint tuple");
    }

    protected ArchitectureModel getArchModel() {
        return Objects.requireNonNull(archModel, "Confidences have not been calculated yet");
    }

    public final NodeResult calculateConfidences(ArchitectureModel archModel, CodeModel codeModel, NodeResult nodeResult) {
        this.nodeResult = nodeResult;
        this.archModel = archModel;
        return getNodeResult(archModel, codeModel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DependentHeuristic that))
            return false;
        if (!super.equals(o))
            return false;

        if (!Objects.equals(nodeResult, that.nodeResult))
            return false;
        return Objects.equals(archModel, that.archModel);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (nodeResult != null ? nodeResult.hashCode() : 0);
        result = 31 * result + (archModel != null ? archModel.hashCode() : 0);
        return result;
    }
}
