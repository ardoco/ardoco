/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.ComputationResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.heuristics.DependentHeuristic;

/**
 * A computation node that has a dependent heuristic as function. This node has
 * exactly one child node.
 */
public class DependentHeuristicNode extends HeuristicNode {

    private DependentHeuristic dependentHeuristic;

    /**
     * Creates a dependent heuristic node with the specified dependent heuristic as
     * function. The node has exactly one child node on which it depends.
     *
     * @param dependentHeuristic the dependent heuristic that the created node uses
     * @param child              the node's child
     */
    public DependentHeuristicNode(DependentHeuristic dependentHeuristic, Node child) {
        super(child);
        this.dependentHeuristic = dependentHeuristic;
    }

    @Override
    public NodeResult calculateConfidences(ArchitectureModel archModel, CodeModel codeModel, ComputationResult result) {
        return dependentHeuristic.calculateConfidences(archModel, codeModel, result.getNodeResult(getChild()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependentHeuristic, getChild());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DependentHeuristicNode other = (DependentHeuristicNode) obj;
        return Objects.equals(dependentHeuristic, other.dependentHeuristic) && Objects.equals(getChild(), other.getChild());
    }

    @Override
    public String getMethodName() {
        return dependentHeuristic.toString();
    }

    @Override
    public String toString() {
        Node child = getChild();
        String childString = ", Child: " + child.getMethodName();
        return dependentHeuristic.toString() + childString;
    }
}
