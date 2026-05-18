/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

/**
 * The Pipeline defines and manages the execution of agents.
 */
public final class Ardoco extends Pipeline {

    private static final Logger classLogger = LoggerFactory.getLogger(Ardoco.class);

    private final String projectName;

    /**
     * Default constructor to simplify tests that do not require the project's name. Also needed for testing configurations.
     */
    @SuppressWarnings("unused")
    private Ardoco() {
        this("");
    }

    /**
     * Creates a new instance of ARDoCo. The provided name should be the project's name and will be used to identify locations within the text where the project
     * is mentioned.
     *
     * @param projectName the project's name
     */
    public Ardoco(String projectName) {
        super("ARDoCo", new DataRepository());
        this.projectName = projectName;
        this.initDataRepository();
    }

    private void initDataRepository() {
        ProjectPipelineData projectPipelineData = new ProjectPipelineDataImpl(this.projectName);
        this.getDataRepository().addData(ProjectPipelineData.ID, projectPipelineData);
    }

    @Override
    public DataRepository getDataRepository() {
        return super.getDataRepository();
    }

    /**
     * Runs the ARDoCo pipeline and saves the results to the specified output directory.
     *
     * @param outputDir the directory where output files should be saved
     * @return the ARDoCo result containing all analysis data, or null if the pipeline is not properly initialized
     */
    public ArdocoResult runAndSave(File outputDir) {
        classLogger.info("Starting {}", this.projectName);

        if (!this.hasPipelineSteps()) {
            this.getLogger().error("Pipeline has not been defined and initialized beforehand. Aborting!");
            return null;
        }

        var startTime = Instant.now();
        this.run();
        var endTime = Instant.now();

        ArdocoResult arDoCoResult = new ArdocoResult(this.getDataRepository());
        saveOutput(this.projectName, outputDir, arDoCoResult);

        if (this.getLogger().isInfoEnabled()) {
            var duration = Duration.between(startTime, endTime);
            long minutesPart = duration.toMinutes();
            int secondsPart = duration.toSecondsPart();
            int millisPart = duration.toMillisPart();
            String durationString = String.format("%02d:%02d.%03d", minutesPart, secondsPart, millisPart);
            classLogger.info(durationString);
        }
        return arDoCoResult;
    }

    private static void saveOutput(String name, File outputDir, ArdocoResult arDoCoResult) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(outputDir);
        Objects.requireNonNull(arDoCoResult);

        classLogger.info("Starting to write output...");
        FilePrinter.writeTraceabilityLinkRecoveryOutput(getOutputFile(name, outputDir, "traceLinks_"), arDoCoResult);
        FilePrinter.writeTraceLinksAsCsv(arDoCoResult, outputDir);
        FilePrinter.writeInconsistencyOutput(getOutputFile(name, outputDir, "inconsistencyDetection_"), arDoCoResult);
        classLogger.info("Finished to write output.");
    }

    private static File getOutputFile(String name, File outputDir, String prefix) {
        var filename = prefix + name + ".txt";
        var filepath = outputDir.toPath().resolve(filename);
        return filepath.toFile();
    }

}
