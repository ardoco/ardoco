/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree;

/**
 * A computation node that has a heuristic as function. This node has no
 * children or exactly one child.
 */
public abstract class HeuristicNode extends Node {

    /**
     * Creates a heuristic node with no children.
     */
    protected HeuristicNode() {
    }

    /**
     * Creates a heuristic node with the specified child.
     *
     * @param child the child of the heuristic node to be created
     */
    protected HeuristicNode(Node child) {
        super(child);
    }
}
