/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

/**
 * The Pipeline defines the execution of the agents.
 */
public final class ArDoCo extends Pipeline {

    private static final Logger classLogger = LoggerFactory.getLogger(ArDoCo.class);

    private final String projectName;

    /**
     * Default constructor to adhere simplify tests that do not care about the project's name. Additionally, it is needed for testing the configurations
     */
    @SuppressWarnings("unused")
    private ArDoCo() {
        this("");
    }

    /**
     * Creates a new instance of ArDoCo. The provided name should be the project's name and will be used to identify spots within the text where the project is
     * mentioned.
     *
     * @param projectName the project's name
     */
    public ArDoCo(String projectName) {
        super("ArDoCo", new DataRepository());
        this.projectName = projectName;
        this.initDataRepository();
    }

    /**
     * Returns a new instance of this class based with the given project name
     *
     * @param projectName the project name
     * @return a new instance of ArDoCo
     */
    public static ArDoCo getInstance(String projectName) {
        return new ArDoCo(projectName);
    }

    private void initDataRepository() {
        ProjectPipelineData projectPipelineData = new ProjectPipelineDataImpl(this.projectName);
        this.getDataRepository().addData(ProjectPipelineData.ID, projectPipelineData);
    }

    @Override
    public DataRepository getDataRepository() {
        return super.getDataRepository();
    }

    public ArDoCoResult runAndSave(File outputDir) {
        classLogger.info("Starting {}", this.projectName);

        if (!this.hasPipelineSteps()) {
            this.getLogger().error("Pipeline has not been defined and initialized beforehand. Aborting!");
            return null;
        }

        var startTime = Instant.now();
        this.run();
        var endTime = Instant.now();

        ArDoCoResult arDoCoResult = new ArDoCoResult(this.getDataRepository());
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

    private static void saveOutput(String name, File outputDir, ArDoCoResult arDoCoResult) {
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
