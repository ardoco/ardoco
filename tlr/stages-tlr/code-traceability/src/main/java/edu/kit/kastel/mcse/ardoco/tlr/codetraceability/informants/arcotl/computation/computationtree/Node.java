/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.ComputationResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;

/**
 * A computation node in a computation tree.
 */
public abstract class Node {

    private List<Node> children;

    /**
     * Creates a computation node without children nodes, i.e. a leaf in the
     * computation tree.
     */
    protected Node() {
        this.children = new ArrayList<>();
    }

    /**
     * Creates a computation node with exactly one child node.
     *
     * @param child the child of the node to be created
     */
    protected Node(Node child) {
        this.children = new ArrayList<>();
        children.add(child);
    }

    /**
     * Creates a computation node with any number of children nodes.
     *
     * @param children the children of the node to be created
     */
    protected Node(List<Node> children) {
        this.children = children;
    }

    /**
     * Returns the children nodes.
     *
     * @return the children nodes
     */
    public List<Node> getChildren() {
        return children;
    }

    /**
     * Returns the child node. Throws an {@code IllegalStateException} if not
     * exactly one child exists.
     *
     * @return the child node
     * @throws IllegalStateException if not exactly one child exists
     */
    public Node getChild() {
        if (children.size() != 1) {
            throw new IllegalStateException("Not exactly one child exists");
        }
        return children.getFirst();
    }

    /**
     * Returns the computed results of all nodes in the subtree rooted at this node.
     * A computation node's result are the calculated confidences of every endpoint
     * tuple using the node's function, i.e. heuristic or aggregation. An endpoint
     * tuple has one endpoint in the specified architecture model and one in the
     * specified code model. For this node as well as every descendant of this node
     * the result is computed if it doesn't already exist. As every parent node's
     * result depends on the parent's children's results, the children's results are
     * always computed prior to the parent's result. For better performance the
     * specified existing result can contain the results of nodes that don't need to
     * get computed again.
     *
     * @param archModel      the architecture model for which confidences will be
     *                       calculated
     * @param codeModel      the code model for which confidences will be calculated
     * @param existingResult a partial computation result that already exists and
     *                       therefore doesn't need to be computed again
     * @return the results of all computation nodes in the subtree rooted at this
     *         node
     */
    public ComputationResult compute(ArchitectureModel archModel, CodeModel codeModel, ComputationResult existingResult) {
        ComputationResult result = new ComputationResult();
        for (Node child : children) {
            if (!result.exists(child)) {
                ComputationResult childrenResult = child.compute(archModel, codeModel, existingResult);
                result.addAll(childrenResult);
            }
        }
        if (existingResult.exists(this)) {
            result.addNodeResult(this, existingResult.getNodeResult(this));
            return result;
        }
        NodeResult nodeResult = calculateConfidences(archModel, codeModel, result);
        result.addNodeResult(this, nodeResult);
        existingResult.addNodeResult(this, nodeResult);
        return result;
    }

    /**
     * Calculates and returns the result of this computation node. The calculation
     * is specified by this nodes's function, i.e. heuristic or aggregation and uses
     * the specified computation result. For each endpoint tuple with endpoints in
     * the specified architecture and code models a confidence is
     * calculated.
     *
     * @param archModel the architecture model for which confidences will be
     *                  calculated
     * @param codeModel the code model for which confidences will be calculated
     * @param result    the computation result that is used in the confidence
     *                  calculation
     * @return the calculated confidences for each endpoint tuple with endpoints in
     *         the specified architecture and code models
     */
    public abstract NodeResult calculateConfidences(ArchitectureModel archModel, CodeModel codeModel, ComputationResult result);

    public abstract String getMethodName();
}
