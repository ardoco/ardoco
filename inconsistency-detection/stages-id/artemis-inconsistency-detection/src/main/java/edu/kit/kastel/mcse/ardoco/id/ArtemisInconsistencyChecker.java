package edu.kit.kastel.mcse.ardoco.id;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ArtemisInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.id.agents.ArtemisInconsistencyAgent;
import edu.kit.kastel.mcse.ardoco.id.states.ArtemisInconsistencyStatesImpl;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ArtemisInconsistencyChecker extends AbstractExecutionStage {

    private final List<ArtemisNerStrategy> strategies;

    public ArtemisInconsistencyChecker(DataRepository dataRepository, List<ArtemisNerStrategy> strategies) {
        super(ArtemisInconsistencyAgent.createAgents(dataRepository, strategies), "ArtemisInconsistencyChecker", dataRepository);
        this.strategies = List.copyOf(strategies);
    }

    /**
     * Creates an {@link ArtemisInconsistencyChecker} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @param strategies        the ArTEMiS strategies
     * @return an instance of ArtemisInconsistencyChecker
     */
    public static ArtemisInconsistencyChecker get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository,
            List<ArtemisNerStrategy> strategies) {
        var inconsistencyChecker = new ArtemisInconsistencyChecker(dataRepository, strategies);
        inconsistencyChecker.applyConfiguration(additionalConfigs);
        return inconsistencyChecker;
    }

    @Override
    protected void initializeState() {
        var targets = this.strategies.stream().map(ArtemisNerStrategy::getTarget).toList();
        var inconsistencyStates = ArtemisInconsistencyStatesImpl.build(targets);
        getDataRepository().addData(ArtemisInconsistencyStates.ID, inconsistencyStates);
    }
}
