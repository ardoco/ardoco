/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.functions.aggregation;

import java.util.ArrayList;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.entity.Entity;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.NodeResult;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.AggregationNode;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.arcotl.computation.computationtree.Node;

// filter every node after the first from the first
@Deterministic
public class Filter extends Matcher {

    private boolean filterAlways;

    private static final Filter filterArch = new Filter(EndpointType.ARCHITECTURE, false);
    private static final Filter filterCode = new Filter(EndpointType.CODE, false);
    private static final Filter filterAlway = new Filter(EndpointType.ARCHITECTURE, true);

    private Filter(EndpointType endpointsToUse, boolean filterAlways) {
        super(endpointsToUse);
        this.filterAlways = filterAlways;
    }

    public static AggregationNode getFilterArchNode(Node... children) {
        return new AggregationNode(filterArch, List.of(children));
    }

    public static AggregationNode getFilterCodeNode(Node... children) {
        return new AggregationNode(filterCode, List.of(children));
    }

    public static AggregationNode getFilterAlwaysNode(Node... children) {
        return new AggregationNode(filterAlway, List.of(children));
    }

    @Override
    protected NodeResult matchEndpoint(Entity endpointToMatch, List<NodeResult> childrenResults) {
        NodeResult unfiltered = childrenResults.getFirst().getResultForEndpoint(endpointToMatch);
        NodeResult filtered = new NodeResult();
        filtered.addAll(unfiltered);

        List<NodeResult> resultsToFilter = new ArrayList<>(childrenResults);
        resultsToFilter.removeFirst();
        for (NodeResult resultToFilter : resultsToFilter) {
            filtered = filtered.filter(resultToFilter);
        }

        if (!filterAlways && filtered.getTraceLinks().isEmpty()) {
            return unfiltered;
        }
        return filtered;
    }

    @Override
    public String toString() {
        return "Filter" + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Filter filter))
            return false;
        if (!super.equals(o))
            return false;

        return filterAlways == filter.filterAlways;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (filterAlways ? 1 : 0);
        return result;
    }
}
