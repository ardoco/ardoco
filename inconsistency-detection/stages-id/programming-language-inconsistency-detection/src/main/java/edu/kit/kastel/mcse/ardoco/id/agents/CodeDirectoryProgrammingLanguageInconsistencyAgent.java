/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.NoDefaultConfiguration;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.CodeDirectoryProgrammingLanguageInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

// TODO: Check how to provide the default configuration: its informants depend on the ArtemisNerStrategy, code path and LargeLanguageModel passed to the
// constructor.
@NoDefaultConfiguration
public class CodeDirectoryProgrammingLanguageInconsistencyAgent extends PipelineAgent {

    public CodeDirectoryProgrammingLanguageInconsistencyAgent(DataRepository dataRepository, ArtemisNerStrategy nerStrategy, String codeProjectPath,
            LargeLanguageModel validationAgentLlm) {
        super(List.of(new CodeDirectoryProgrammingLanguageInconsistencyInformant(dataRepository, nerStrategy, codeProjectPath, validationAgentLlm)),
                "CodeDirectoryProgrammingLanguageInconsistencyAgent", dataRepository);
    }
}
