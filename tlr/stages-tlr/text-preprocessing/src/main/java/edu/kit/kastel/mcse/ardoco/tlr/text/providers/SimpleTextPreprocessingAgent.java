/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.text.providers;

import java.util.List;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.tlr.text.providers.informants.simple.SimpleTextProvider;

public class SimpleTextPreprocessingAgent extends PipelineAgent {

    /**
     * Instantiates a new initial text agent.
     *
     * @param data the {@link DataRepository}
     */
    public SimpleTextPreprocessingAgent(DataRepository data) {
        super(List.of(new SimpleTextProvider(data)), SimpleTextPreprocessingAgent.class.getSimpleName(), data);
    }

    /**
     * Creates a {@link CoreNLPProvider} as {@link NlpInformant} and reads the provided text.
     *
     * @param additionalConfigs the additional configuration that should be applied
     * @param dataRepository    the data repository
     * @return a CoreNLPProvider with the provided text read in
     */
    public static SimpleTextPreprocessingAgent get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var textProvider = new SimpleTextPreprocessingAgent(dataRepository);
        textProvider.applyConfiguration(additionalConfigs);
        return textProvider;
    }

}
