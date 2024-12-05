/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.ArCoTLInformant;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public class InitialCodeTraceabilityAgent extends PipelineAgent {

    public InitialCodeTraceabilityAgent(DataRepository dataRepository) {
        super(List.of(new ArCoTLInformant(dataRepository)), InitialCodeTraceabilityAgent.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void initializeState() {
        // empty
    }
}
