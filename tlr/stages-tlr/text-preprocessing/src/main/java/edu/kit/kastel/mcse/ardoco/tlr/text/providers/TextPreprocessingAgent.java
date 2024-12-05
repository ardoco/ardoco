/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers;

import java.util.List;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.CoreNLPProvider;

public class TextPreprocessingAgent extends PipelineAgent {

    /**
     * Instantiates a new initial text agent.
     *
     * @param data the {@link DataRepository}
     */
    public TextPreprocessingAgent(DataRepository data) {
        super(List.of(new CoreNLPProvider(data)), TextPreprocessingAgent.class.getSimpleName(), data);
    }

    /**
     * Creates a {@link CoreNLPProvider} as {@link NlpInformant} and reads the provided text.
     *
     * @param additionalConfigs the additional configuration that should be applied
     * @param dataRepository    the data repository
     * @return a CoreNLPProvider with the provided text read in
     */
    public static TextPreprocessingAgent get(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var textProvider = new TextPreprocessingAgent(dataRepository);
        textProvider.applyConfiguration(additionalConfigs);
        return textProvider;
    }
}
