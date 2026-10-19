package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.CodeModelArchitecturalStylePatternInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class CodeModelArchitecturalStylePatternInconsistencyAgent extends PipelineAgent {

    public CodeModelArchitecturalStylePatternInconsistencyAgent(DataRepository dataRepository, ArtemisNerStrategy nerStrategy) {
        super(List.of(new CodeModelArchitecturalStylePatternInconsistencyInformant(dataRepository, nerStrategy)),
                "CodeModelArchitecturalStylePatternInconsistencyAgent", dataRepository);
    }
}
