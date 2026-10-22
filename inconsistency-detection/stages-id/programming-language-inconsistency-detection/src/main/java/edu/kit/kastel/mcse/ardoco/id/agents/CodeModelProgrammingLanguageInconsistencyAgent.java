/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.CodeModelProgrammingLanguageInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class CodeModelProgrammingLanguageInconsistencyAgent extends PipelineAgent {

    public CodeModelProgrammingLanguageInconsistencyAgent(DataRepository dataRepository, ArtemisNerStrategy nerStrategy) {
        super(List.of(new CodeModelProgrammingLanguageInconsistencyInformant(dataRepository, nerStrategy)), "CodeModelProgrammingLanguageInconsistencyAgent",
                dataRepository);
    }
}
