package edu.kit.kastel.mcse.ardoco.tlr.artemis;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

public class ArtemisPostprocessing extends AbstractExecutionStage {
    private static final Logger logger = LoggerFactory.getLogger(ArtemisPostprocessing.class);

    public ArtemisPostprocessing(DataRepository dataRepository) {
        super(List.of(), ArtemisPostprocessing.class.getSimpleName(), dataRepository);
        logger.info("ArtemisPostprocessing is currently only a dummy and uses no agents."); //TODO evtl wo anders hinschieben?
    }

    public static ArtemisPostprocessing get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var stage = new ArtemisPostprocessing(dataRepository);
        stage.applyConfiguration(additionalConfigs);
        return stage;
    }

    @Override
    protected void initializeState() {
        // empty
    }
}
