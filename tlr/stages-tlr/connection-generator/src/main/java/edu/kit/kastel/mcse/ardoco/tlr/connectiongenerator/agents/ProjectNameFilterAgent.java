/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.informants.ProjectNameInformant;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * This agent should look for {@link RecommendedInstance RecommendedInstances} that contain the project's name and "filters" them by adding a heavy negative
 * probability, thus making the {@link RecommendedInstance} extremely improbable.
 */
public class ProjectNameFilterAgent extends PipelineAgent {

    /**
     * Create the agent.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public ProjectNameFilterAgent(DataRepository dataRepository) {
        super(List.of(new ProjectNameInformant(dataRepository)), "ProjectNameFilterAgent", dataRepository);
    }
}
