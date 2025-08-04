/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.agents.NerAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.agents.NerConnectionAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.llm.LlmSettings;

public class NerConnectionGenerator extends AbstractExecutionStage {
    protected static final String LOGGING_SETUP_DEBUG = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner";

    private LlmSettings llmSettings = LlmSettings.getDefaultSettings();

    public NerConnectionGenerator(DataRepository dataRepository) {
        super(List.of(new NerAgent(dataRepository), new NerConnectionAgent(dataRepository)), NerConnectionGenerator.class.getSimpleName(), dataRepository);
    }

    public static NerConnectionGenerator get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var connectionGenerator = new NerConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(additionalConfigs);
        return connectionGenerator;
    }

    @Override
    protected void initializeState() {
        var activeMetamodels = this.getDataRepository().getData(ModelStates.ID, ModelStates.class).orElseThrow().getMetamodels();
        var connectionStates = NerConnectionStatesImpl.build(activeMetamodels.toArray(Metamodel[]::new));
        connectionStates.setLlmSettings(llmSettings);
        getDataRepository().addData(NerConnectionStates.ID, connectionStates);
    }

    public void setLlmSettings(LlmSettings llmSettings) {
        this.llmSettings = llmSettings;
    }
}
