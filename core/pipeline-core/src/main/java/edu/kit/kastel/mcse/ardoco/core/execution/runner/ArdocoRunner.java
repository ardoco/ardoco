/* Licensed under MIT 2023-2026. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.Ardoco;

/**
 * Abstract runner for ARDoCo pipeline execution.
 */
public abstract class ArdocoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArdocoRunner.class);

    private final Ardoco ardoco;

    private @Nullable File outputDirectory;
    protected boolean isSetUp = false;

    protected ArdocoRunner(String projectName) {
        this.ardoco = new Ardoco(projectName);
        this.outputDirectory = null;
    }

    /**
     * Checks if the runner is properly set up and ready to run.
     *
     * @return true if the runner is set up, false otherwise
     */
    public boolean isSetUp() {
        return this.isSetUp;
    }

    /**
     * Runs the ARDoCo pipeline and saves the results to the output directory.
     *
     * @return the ARDoCo result, or null if the runner is not properly set up
     */
    public final @Nullable ArdocoResult run() {
        if (this.isSetUp() && this.outputDirectory != null) {
            return this.getArdoco().runAndSave(this.outputDirectory);
        } else {
            logger.error("Cannot run ARDoCo because the runner is not properly set up (#run).");
            return null;
        }
    }

    /**
     * Returns the {@link DataRepository} produced by the run. The results are not saved to the output directory.
     *
     * @return the data repository produced by the run
     */
    public final @Nullable DataRepository runWithoutSaving() {
        if (this.isSetUp()) {
            this.getArdoco().run();
            return this.getArdoco().getDataRepository();
        } else {
            logger.error("Cannot run ARDoCo because the runner is not properly set up (#runWithoutSaving).");
            return null;
        }
    }

    /**
     * Returns the ARDoCo instance used by this runner.
     *
     * @return the ARDoCo instance
     */
    public Ardoco getArdoco() {
        return this.ardoco;
    }

    /**
     * Sets the output directory where results will be saved.
     *
     * @param outputDirectory the directory to save output files to
     */
    protected void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
