/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.tlr.codetraceability.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.codetraceability.informants.ArchitectureLinkToCodeLinkTransformerInformant;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * This class transforms architecture trace links to code trace links by remapping the architecture ids (which are code ids) to the compilation units.
 */
public class ArchitectureLinkToCodeLinkTransformerAgent extends PipelineAgent {

    public ArchitectureLinkToCodeLinkTransformerAgent(DataRepository dataRepository) {
        super(List.of(new ArchitectureLinkToCodeLinkTransformerInformant(dataRepository)), ArchitectureLinkToCodeLinkTransformerAgent.class.getSimpleName(),
                dataRepository);
    }

    @Override
    protected void initializeState() {
        // empty
    }
}
