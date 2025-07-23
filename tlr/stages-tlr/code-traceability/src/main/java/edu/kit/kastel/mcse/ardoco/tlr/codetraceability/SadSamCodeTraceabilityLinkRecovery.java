/* Licensed under MIT 2023-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.agents.TransitiveTraceabilityAgent;

public class SadSamCodeTraceabilityLinkRecovery extends AbstractExecutionStage {

    public SadSamCodeTraceabilityLinkRecovery(DataRepository dataRepository) {
        super(List.of(new TransitiveTraceabilityAgent(dataRepository)), SadSamCodeTraceabilityLinkRecovery.class.getSimpleName(), dataRepository);

    }

    public static SadSamCodeTraceabilityLinkRecovery get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var sadSamCodeTraceabilityLinkRecovery = new SadSamCodeTraceabilityLinkRecovery(dataRepository);
        sadSamCodeTraceabilityLinkRecovery.applyConfiguration(additionalConfigs);
        return sadSamCodeTraceabilityLinkRecovery;
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
