/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;

/**
 * This class can be used to easily define an anonymous {@link ArDoCoRunner} for testing purposes.
 */
public abstract class AnonymousRunner extends ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(AnonymousRunner.class);

    protected AnonymousRunner(String projectName) {
        super(projectName);
        setUp(null);
    }

    protected AnonymousRunner(String projectName, DataRepository preRunDataRepository) {
        super(projectName);
        setUp(preRunDataRepository);
    }

    /**
     * Sets up the runner using {@link #initializePipelineSteps}. Initializes the new data repository using the preRunDataRepository, if present.
     * {@link #isSetUp} must return true, if successful.
     *
     * @param preRunDataRepository data repository of a previous run used as a base
     * @return List of AbstractPipelineSteps this runner consists of
     */
    private List<AbstractPipelineStep> setUp(@Nullable DataRepository preRunDataRepository) {
        try {
            var arDoCo = getArDoCo();
            var dataRepository = arDoCo.getDataRepository();

            if (preRunDataRepository != null) {
                dataRepository.addAllData(preRunDataRepository);
            }

            var pipelineSteps = initializePipelineSteps(dataRepository);
            pipelineSteps.forEach(arDoCo::addPipelineStep);
            isSetUp = true;
            return pipelineSteps;
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            isSetUp = false;
            return List.of();
        }
    }

    /**
     * Initializes and returns the pipeline steps according to the supplied parameters
     *
     * @param dataRepository the data repository of this runner
     * @throws IOException can occur when loading data
     */
    public abstract List<AbstractPipelineStep> initializePipelineSteps(DataRepository dataRepository) throws IOException;

    @Override
    public void setOutputDirectory(File outputDirectory) {
        super.setOutputDirectory(outputDirectory);
    }
}
