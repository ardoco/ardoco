/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.execution.runner;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.execution.ArDoCo;

public abstract class ArDoCoRunner {
    private static final Logger logger = LoggerFactory.getLogger(ArDoCoRunner.class);

    private final ArDoCo arDoCo;

    private File outputDirectory;
    protected boolean isSetUp = false;

    protected ArDoCoRunner(String projectName) {
        this.arDoCo = new ArDoCo(projectName);
        this.outputDirectory = null;
    }

    public boolean isSetUp() {
        return this.isSetUp;
    }

    public final ArDoCoResult run() {
        if (this.isSetUp() && this.outputDirectory != null) {
            return this.getArDoCo().runAndSave(this.outputDirectory);
        } else {
            logger.error("Cannot run ArDoCo because the runner is not properly set up (#run).");
            return null;
        }
    }

    /**
     * {@return the {@link DataRepository} produced by the run} The results are not saved to the output directory.
     */
    public final DataRepository runWithoutSaving() {
        if (this.isSetUp()) {
            this.getArDoCo().run();
            return this.getArDoCo().getDataRepository();
        } else {
            logger.error("Cannot run ArDoCo because the runner is not properly set up (#runWithoutSaving).");
            return null;
        }
    }

    public ArDoCo getArDoCo() {
        return this.arDoCo;
    }

    protected void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
