/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.aggregation;

import java.util.List;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.Confidence;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.AggregationNode;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.Node;

public class Threshold extends ConfidenceAggregator {

    private final double limit;

    private Threshold(double threshold) {
        if (!(threshold >= 0 && threshold <= 1)) {
            throw new IllegalArgumentException("Threshold must not be smaller than 0 or bigger than 1");
        }
        this.limit = threshold;
    }

    /**
     * Returns a new aggregation node with the specified children nodes. The aggregation function uses the specified threshold. The threshold must be between 0
     * and 1, or an
     * {@code IllegalArgumentException} gets thrown.
     *
     * @param threshold the threshold of the aggregation function,
     *                  must be between 0 and 1
     * @param children  the node's children on whose results the threshold is applied
     * @throws IllegalArgumentException if the threshold is smaller than 0 or bigger
     *                                  than 1
     */
    public static AggregationNode getThresholdNode(double threshold, Node... children) {
        Threshold aggregation = new Threshold(threshold);
        return new AggregationNode(aggregation, List.of(children));
    }

    @Override
    protected Confidence aggregateConfidences(List<Confidence> confidences) {
        Confidence bestConfidence = new Confidence();
        for (Confidence confidence : confidences) {
            if (confidence.compareTo(bestConfidence) > 0) {
                bestConfidence = confidence;
            }
        }
        Confidence thresholdConfidence = new Confidence(limit);
        if (bestConfidence.compareTo(thresholdConfidence) >= 0) {
            return bestConfidence;
        }
        return new Confidence();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), limit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Threshold other = (Threshold) obj;
        return limit == other.limit;
    }

    @Override
    public String toString() {
        return "Threshold-" + limit;
    }
}
