package edu.kit.kastel.mcse.ardoco.tlr.artemis;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.SimpleTextPreprocessingAgent;

public class ArtemisPreprocessing extends AbstractExecutionStage {

    public ArtemisPreprocessing(DataRepository dataRepository, ImmutableSortedMap<String, String> additionalConfigs,
            @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration) {
        super(List.of(new SimpleTextPreprocessingAgent(dataRepository), new ModelProviderAgent(dataRepository, architectureConfiguration, codeConfiguration)),
                ArtemisPreprocessing.class.getSimpleName(), dataRepository);
    }

    public static ArtemisPreprocessing get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository,
            @Nullable ArchitectureConfiguration architectureConfiguration, @Nullable CodeConfiguration codeConfiguration) {
        var stage = new ArtemisPreprocessing(dataRepository, additionalConfigs, architectureConfiguration, codeConfiguration);
        stage.applyConfiguration(additionalConfigs);
        return stage;
    }

    @Override
    protected void initializeState() {
        // empty
    }
}
