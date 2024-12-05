/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.aggregation;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.AggregationNode;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.Node;

// If the endpoint E1 is linked (i.e. has a confidence with a value) to different endpoints by the children,
// only the links from the first child with a link for the endpoint E1 is used.
public class MatchSequentially extends Matcher {

    private static final MatchSequentially matchSeqArch = new MatchSequentially(EndpointType.ARCHITECTURE);
    private static final MatchSequentially matchSeqCode = new MatchSequentially(EndpointType.CODE);

    private MatchSequentially(EndpointType endpointsToUse) {
        super(endpointsToUse);
    }

    public static AggregationNode getMatchSeqArchNode(Node... children) {
        return new AggregationNode(matchSeqArch, List.of(children));
    }

    public static AggregationNode getMatchSeqCodeNode(Node... children) {
        return new AggregationNode(matchSeqCode, List.of(children));
    }

    @Override
    protected NodeResult matchEndpoint(Entity endpointToMatch, List<NodeResult> childrenResults) {
        for (NodeResult childResult : childrenResults) {
            if (childResult.hasTraceLink(endpointToMatch)) {
                return childResult.getResultForEndpoint(endpointToMatch);
            }
        }
        return new NodeResult();
    }

    @Override
    public String toString() {
        return "Sequential" + super.toString();
    }
}
