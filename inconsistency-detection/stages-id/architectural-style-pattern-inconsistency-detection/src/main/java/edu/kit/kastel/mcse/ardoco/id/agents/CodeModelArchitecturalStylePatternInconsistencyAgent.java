/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.CodeModelArchitecturalStylePatternInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class CodeModelArchitecturalStylePatternInconsistencyAgent extends PipelineAgent {

    public CodeModelArchitecturalStylePatternInconsistencyAgent(DataRepository dataRepository, ArtemisNerStrategy nerStrategy,
            LargeLanguageModel validationAgentLlm) {
        super(List.of(new CodeModelArchitecturalStylePatternInconsistencyInformant(dataRepository, nerStrategy, validationAgentLlm)),
                "CodeModelArchitecturalStylePatternInconsistencyAgent", dataRepository);
    }
}
