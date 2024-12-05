/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.ComputationResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.aggregation.Aggregation;

/**
 * A computation node that has an aggregation as function. This node has at
 * least one child node.
 */
public class AggregationNode extends Node {

    private final Aggregation aggregation;

    /**
     * Creates an aggregation node with the specified aggregation as function and
     * the specified children nodes to be aggregated. The number of children nodes
     * must be at least one, or an {@code IllegalArgumentException} gets thrown.
     *
     * @param aggregation the aggregation that the created node uses
     * @param children    the node's children to be aggregated, must not be an empty
     *                    list
     * @throws IllegalArgumentException if the number of children nodes is zero
     */
    public AggregationNode(Aggregation aggregation, List<Node> children) {
        super(children);
        if (children.isEmpty()) {
            throw new IllegalArgumentException("The number of children nodes must not be zero");
        }
        this.aggregation = aggregation;
    }

    @Override
    public NodeResult calculateConfidences(ArchitectureModel archModel, CodeModel codeModel, ComputationResult result) {
        List<NodeResult> childrenResults = new ArrayList<>();
        for (Node child : getChildren()) {
            childrenResults.add(result.getNodeResult(child));
        }
        return aggregation.calculateConfidences(archModel, codeModel, childrenResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregation, getChildren());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AggregationNode other = (AggregationNode) obj;
        return Objects.equals(aggregation, other.aggregation) && Objects.equals(getChildren(), other.getChildren());
    }

    @Override
    public String getMethodName() {
        return aggregation.toString();
    }

    @Override
    public String toString() {
        StringBuilder childrenStringBuilder = new StringBuilder(", Children:");
        for (Node child : getChildren()) {
            childrenStringBuilder.append(" ").append(child.getMethodName());
        }
        return aggregation.toString() + childrenStringBuilder;
    }
}
