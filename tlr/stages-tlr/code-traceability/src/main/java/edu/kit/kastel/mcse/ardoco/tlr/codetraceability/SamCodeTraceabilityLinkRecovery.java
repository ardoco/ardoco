/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability;

import java.util.List;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.agents.InitialCodeTraceabilityAgent;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

public class SamCodeTraceabilityLinkRecovery extends AbstractExecutionStage {

    public SamCodeTraceabilityLinkRecovery(DataRepository dataRepository) {
        super(List.of(new InitialCodeTraceabilityAgent(dataRepository)), SamCodeTraceabilityLinkRecovery.class.getSimpleName(), dataRepository);
    }

    public static SamCodeTraceabilityLinkRecovery get(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var samCodeTraceabilityLinkRecovery = new SamCodeTraceabilityLinkRecovery(dataRepository);
        samCodeTraceabilityLinkRecovery.applyConfiguration(additionalConfigs);
        return samCodeTraceabilityLinkRecovery;
    }

    @Override
    protected void initializeState() {
        DataRepository dataRepository = getDataRepository();
        if (!DataRepositoryHelper.hasCodeTraceabilityState(dataRepository)) {
            var codeTraceabilityState = new CodeTraceabilityStateImpl();
            dataRepository.addData(CodeTraceabilityState.ID, codeTraceabilityState);
        }
    }
}
