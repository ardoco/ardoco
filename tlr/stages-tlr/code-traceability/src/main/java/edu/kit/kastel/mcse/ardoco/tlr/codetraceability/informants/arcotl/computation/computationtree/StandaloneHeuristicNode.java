/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.ComputationResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics.StandaloneHeuristic;

/**
 * A computation node that has a standalone heuristic as function. This node has
 * no children nodes.
 */
public class StandaloneHeuristicNode extends HeuristicNode {

    private StandaloneHeuristic standaloneHeuristic;

    /**
     * Creates a standalone heuristic node with the specified standalone heuristic
     * as function. The node has no children nodes and is therefore a leaf in the
     * computation tree.
     *
     * @param standaloneHeuristic the standalone heuristic that the created node
     *                            uses
     */
    public StandaloneHeuristicNode(StandaloneHeuristic standaloneHeuristic) {
        this.standaloneHeuristic = standaloneHeuristic;
    }

    @Override
    public NodeResult calculateConfidences(ArchitectureModel archModel, CodeModel codeModel, ComputationResult result) {
        return standaloneHeuristic.calculateConfidences(archModel, codeModel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(standaloneHeuristic);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        StandaloneHeuristicNode other = (StandaloneHeuristicNode) obj;
        return Objects.equals(standaloneHeuristic, other.standaloneHeuristic);
    }

    @Override
    public String getMethodName() {
        return standaloneHeuristic.toString();
    }

    @Override
    public String toString() {
        return standaloneHeuristic.toString();
    }
}
