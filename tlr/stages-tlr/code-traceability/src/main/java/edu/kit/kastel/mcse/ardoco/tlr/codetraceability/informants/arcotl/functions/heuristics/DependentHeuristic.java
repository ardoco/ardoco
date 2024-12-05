/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.DependentHeuristicNode;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.Node;

/**
 * A heuristic that depends on exactly one computation node's result. To apply
 * this heuristic there need to already exist some calculated confidences that
 * can be used.
 */
public abstract class DependentHeuristic extends Heuristic {

    private NodeResult nodeResult;
    private ArchitectureModel archModel;

    public DependentHeuristicNode getNode(Node child) {
        return new DependentHeuristicNode(this, child);
    }

    protected NodeResult getNodeResult() {
        return nodeResult;
    }

    protected ArchitectureModel getArchModel() {
        return archModel;
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
