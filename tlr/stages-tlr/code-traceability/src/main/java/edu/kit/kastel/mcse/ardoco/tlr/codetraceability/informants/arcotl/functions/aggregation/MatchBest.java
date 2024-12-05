/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.aggregation;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.AggregationNode;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.Node;

public class MatchBest extends Matcher {

    private static final MatchBest matchBestArch = new MatchBest(EndpointType.ARCHITECTURE);
    private static final MatchBest matchBestCode = new MatchBest(EndpointType.CODE);

    private MatchBest(EndpointType endpointsToUse) {
        super(endpointsToUse);
    }

    public static AggregationNode getMatchBestArchNode(Node... children) {
        return new AggregationNode(matchBestArch, List.of(children));
    }

    public static AggregationNode getMatchBestCodeNode(Node... children) {
        return new AggregationNode(matchBestCode, List.of(children));
    }

    @Override
    protected NodeResult matchEndpoint(Entity endpointToMatch, List<NodeResult> childrenResults) {
        Confidence bestConfidence = new Confidence();
        for (NodeResult childResult : childrenResults) {
            Confidence childBestConfidence = (childResult.getBestConfidence(endpointToMatch));
            if (childBestConfidence.compareTo(bestConfidence) > 0) {
                bestConfidence = childBestConfidence;
            }
        }

        if (!bestConfidence.hasValue()) {
            return new NodeResult();
        }

        NodeResult partialMatchResult = new NodeResult();
        for (NodeResult childResult : childrenResults) {
            NodeResult childBest = childResult.getEndpointTuples(endpointToMatch, bestConfidence);
            partialMatchResult.addAll(childBest);
        }

        return partialMatchResult;
    }

    @Override
    public String toString() {
        return "Best" + super.toString();
    }
}
