/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.aggregation;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.AggregationNode;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.Node;

public class Maximum extends ConfidenceAggregator {

    private static final Maximum max = new Maximum();

    public static AggregationNode getMaximumNode(Node... children) {
        return new AggregationNode(max, List.of(children));
    }

    @Override
    protected Confidence aggregateConfidences(List<Confidence> confidences) {
        Confidence maxConfidence = new Confidence();
        for (Confidence confidence : confidences) {
            if (confidence.compareTo(maxConfidence) > 0) {
                maxConfidence = confidence;
            }
        }
        return maxConfidence;
    }

    @Override
    public String toString() {
        return "Maximum";
    }
}
