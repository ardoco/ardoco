package edu.kit.kastel.mcse.ardoco.tlr.artemis;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.agents.ArtemisNerAgent;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisTraceabilityStates;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisTraceabilityStatesImpl;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class ArtemisNer extends AbstractExecutionStage {

    private final List<ArtemisNerStrategy> strategies;

    public ArtemisNer(DataRepository dataRepository, LargeLanguageModel llm, List<ArtemisNerStrategy> strategies) {
        super(strategies.stream().map(strategy -> new ArtemisNerAgent(dataRepository, llm, strategy)).toList(), ArtemisNer.class.getSimpleName(),
                dataRepository);
        this.strategies = List.copyOf(strategies);
    }

    public static ArtemisNer get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository, LargeLanguageModel llm,
            List<ArtemisNerStrategy> strategies) {
        var stage = new ArtemisNer(dataRepository, llm, strategies);
        stage.applyConfiguration(additionalConfigs);
        return stage;
    }

    @Override
    protected void initializeState() {
        var existingStates = getDataRepository().getData(ArtemisTraceabilityStates.ID, ArtemisTraceabilityStatesImpl.class);
        if (existingStates.isPresent()) {
            for (ArtemisNerStrategy strategy : strategies) {
                existingStates.get().getState(strategy.getTarget());
            }
            return;
        }

        List<ArtemisTarget> targets = strategies.stream().map(ArtemisNerStrategy::getTarget).toList();
        getDataRepository().addData(ArtemisTraceabilityStates.ID, ArtemisTraceabilityStatesImpl.build(targets));
    }
}
