/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.informants.MappingCombinerInformant;

public class MappingCombiner extends PipelineAgent {
    public MappingCombiner(DataRepository dataRepository) {
        super(List.of(new MappingCombinerInformant(dataRepository)), MappingCombiner.class.getSimpleName(), dataRepository);
    }
}
