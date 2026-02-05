/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.id;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.InconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.id.agents.InitialInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.id.agents.ModelEntityAbsentFromTextInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.id.agents.TextEntityAbsentFromModelInconsistencyAgent;

public class InconsistencyChecker extends AbstractExecutionStage {

    public InconsistencyChecker(DataRepository dataRepository) {
        super(List.of(new InitialInconsistencyAgent(dataRepository), new TextEntityAbsentFromModelInconsistencyAgent(dataRepository),
                new ModelEntityAbsentFromTextInconsistencyAgent(dataRepository)), "InconsistencyChecker", dataRepository);
    }

    /**
     * Creates an {@link InconsistencyChecker} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of InconsistencyChecker
     */
    public static InconsistencyChecker get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var inconsistencyChecker = new InconsistencyChecker(dataRepository);
        inconsistencyChecker.applyConfiguration(additionalConfigs);
        return inconsistencyChecker;
    }

    @Override
    protected void initializeState() {
        var inconsistencyStates = InconsistencyStatesImpl.build();
        getDataRepository().addData(InconsistencyStates.ID, inconsistencyStates);
    }

}
