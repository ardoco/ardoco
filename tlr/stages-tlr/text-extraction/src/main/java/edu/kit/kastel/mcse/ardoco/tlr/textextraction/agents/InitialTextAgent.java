/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.informants.InDepArcsInformant;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.informants.NounInformant;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.informants.OutDepArcsInformant;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.informants.SeparatedNamesInformant;

/**
 * The Class InitialTextAgent defines the agent that executes the extractors for the text stage.
 */
public class InitialTextAgent extends PipelineAgent {

    /**
     * Instantiates a new initial text agent.
     *
     * @param data the {@link DataRepository}
     */
    public InitialTextAgent(DataRepository data) {
        super(List.of(new NounInformant(data), new InDepArcsInformant(data), new OutDepArcsInformant(data), new SeparatedNamesInformant(data)),
                InitialTextAgent.class.getSimpleName(), data);
    }
}
