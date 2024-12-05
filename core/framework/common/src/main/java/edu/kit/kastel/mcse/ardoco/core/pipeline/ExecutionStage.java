/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.util.List;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

public abstract class ExecutionStage extends AbstractExecutionStage {
    private SortedMap<String, String> additionalConfigs;

    /**
     * Creates an {@link ExecutionStage} and applies the additional configuration to it
     *
     * @param id                the id of the stage
     * @param dataRepository    the {@link DataRepository} that should be used
     * @param agents            the pipeline agents this stage supports
     * @param additionalConfigs the additional configuration
     */
    protected ExecutionStage(List<PipelineAgent> agents, String id, DataRepository dataRepository, SortedMap<String, String> additionalConfigs) {
        super(agents, id, dataRepository);
        this.additionalConfigs = additionalConfigs;
    }

    @Override
    protected void before() {
        super.before();
        this.applyConfiguration(this.additionalConfigs);
    }
}
