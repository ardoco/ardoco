/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.artemis;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.agents.ArtemisConnectionAgent;
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
    protected void initializeState() {
    }
}
