/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.CodeDirectoryArchitecturalStylePatternInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class CodeDirectoryArchitecturalStylePatternInconsistencyAgent extends PipelineAgent {

    public CodeDirectoryArchitecturalStylePatternInconsistencyAgent(DataRepository dataRepository, ArtemisNerStrategy nerStrategy, String codeProjectPath) {
        super(List.of(new CodeDirectoryArchitecturalStylePatternInconsistencyInformant(dataRepository, nerStrategy, codeProjectPath)),
                "CodeDirectoryArchitecturalStylePatternInconsistencyAgent", dataRepository);
    }
}
