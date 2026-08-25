package edu.kit.kastel.mcse.ardoco.tlr.artemis;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.agents.ArtemisConnectionAgent;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisTraceabilityStatesImpl;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ArtemisConnectionGenerator extends AbstractExecutionStage {

    private final List<ArtemisNerStrategy> strategies;

    public ArtemisConnectionGenerator(DataRepository dataRepository, List<ArtemisNerStrategy> strategies) {
        super(strategies.stream().map(strategy -> new ArtemisConnectionAgent(dataRepository, strategy)).toList(),
                ArtemisConnectionGenerator.class.getSimpleName(), dataRepository);
        this.strategies = List.copyOf(strategies);
    }

    public static ArtemisConnectionGenerator get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository,
            List<ArtemisNerStrategy> strategies) {
        var stage = new ArtemisConnectionGenerator(dataRepository, strategies);
        stage.applyConfiguration(additionalConfigs);
        return stage;
    }

    @Override
    protected void initializeState() { //TODO fix duplicated code bad-smell!
        var existingStates = getDataRepository().getData(ArtemisConnectionStates.ID, ArtemisTraceabilityStatesImpl.class);
        if (existingStates.isPresent()) {
            for (ArtemisNerStrategy strategy : strategies) {
                existingStates.get().getState(strategy.getTarget());
            }
            return;
        }

        List<ArtemisTarget> targets = strategies.stream().map(ArtemisNerStrategy::getTarget).toList();
        getDataRepository().addData(ArtemisConnectionStates.ID, ArtemisTraceabilityStatesImpl.build(targets));
    }
}
