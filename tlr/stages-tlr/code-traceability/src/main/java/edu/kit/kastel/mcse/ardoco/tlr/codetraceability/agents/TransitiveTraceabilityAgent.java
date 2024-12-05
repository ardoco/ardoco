/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.TraceLinkCombiner;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class TransitiveTraceabilityAgent extends PipelineAgent {

    public TransitiveTraceabilityAgent(DataRepository dataRepository) {
        super(List.of(new TraceLinkCombiner(dataRepository)), TransitiveTraceabilityAgent.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void initializeState() {
        // empty
    }
}
