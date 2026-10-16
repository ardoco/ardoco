/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceStorage;
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.core.common.util.FilePrinter;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

/**
 * The Pipeline defines and manages the execution of agents.
 */
public final class Ardoco extends Pipeline {

    private static final Logger classLogger = LoggerFactory.getLogger(Ardoco.class);

    /** Config key read from {@link PersistenceBridge#getInstance()}'s last applied configuration; see {@link #resolveConfiguredExportDirectory}. */
    private static final String CONFIGURED_EXPORT_PATH_KEY = "PersistenceBridge::knowledgeGraphExportPath";

    private static final String CONFIGURED_EXPORT_PATH_ENV_VAR = "ARDOCO_KNOWLEDGE_GRAPH_EXPORT_PATH";

    /** Fallback export directory, relative to the current working directory, when nothing else is configured. */
    private static final String DEFAULT_EXPORT_DIRECTORY = "knowledge-graph";

    private final String projectName;

    /** Set by {@link #runAndSave} right before {@link #run()}; read and cleared by {@link #exportAndWipeKnowledgeGraph}. */
    private @Nullable File pendingKnowledgeGraphExportDirectory;

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
     * Runs the pipeline. In Neo4j mode ({@code PersistenceBridge::storage=neo4j}), this is the single lifecycle point
     * for both {@link #runAndSave} and a bare {@code run()} (e.g. via {@code ArdocoRunner#runWithoutSaving}, which
     * has no output directory): clean the database first (a crashed earlier run cannot clean up after itself), run
     * the pipeline, then on success export the knowledge graph and wipe, or on failure wipe (best-effort) and
     * rethrow so the run aborts with no partial results, matching the {@link DataRepository}-only failsafe. The
     * export destination is {@link #runAndSave}'s output directory if this call came from there, otherwise
     * {@link #resolveConfiguredExportDirectory}, which always resolves to a usable directory (falling back to a
     * default one) so a run never silently loses its one deliverable.
     */
    @Override
    public void run() {
        boolean neo4jMode = PersistenceBridge.storageStatic == PersistenceStorage.NEO4J;
        if (neo4jMode) {
            classLogger.info("Neo4j mode: cleaning the database before the run (a crashed earlier run may have left data behind)");
            PersistenceBridge.getHandler().deleteAllData();
        }

        try {
            super.run();
        } catch (RuntimeException | Error ex) {
            if (neo4jMode) {
                wipeAfterFailure(ex);
            }
            throw ex;
        }

        if (neo4jMode) {
            exportAndWipeKnowledgeGraph();
        }
    }

    /**
     * Runs the ARDoCo pipeline and saves the results to the specified output directory. The Neo4j-mode lifecycle
     * (clean/export/wipe) runs inside {@link #run()}; this method only has to tell it where to export.
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

        this.pendingKnowledgeGraphExportDirectory = outputDir;
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

    /**
     * Writes the run's knowledge graph as a lossless JSON dump, then wipes the database: in Neo4j mode, Neo4j is a
     * transient per-run store like the {@link DataRepository}, so nothing but the exported graph survives. The
     * export directory is {@link #runAndSave}'s output directory if set, otherwise
     * {@link #resolveConfiguredExportDirectory} - which always returns a usable directory, falling back to a default
     * one, so the wipe below never discards the graph silently. Both steps are unguarded (no {@code runQuietly}): a
     * failure here means the run's one deliverable - the knowledge graph - was not safely captured, so the run must
     * abort rather than silently lose it.
     */
    private void exportAndWipeKnowledgeGraph() {
        File exportDirectory = this.pendingKnowledgeGraphExportDirectory != null ? this.pendingKnowledgeGraphExportDirectory
                : resolveConfiguredExportDirectory();
        this.pendingKnowledgeGraphExportDirectory = null;

        classLogger.info("Exporting the Neo4j knowledge graph...");
        String json = PersistenceBridge.getHandler().exportGraphAsJson();
        File file = getOutputFile(this.projectName, exportDirectory, "knowledgeGraph_", ".json");
        try {
            Files.createDirectories(exportDirectory.toPath());
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write the knowledge graph export to " + file, e);
        }
        classLogger.info("Wrote the knowledge graph to {}", file);

        classLogger.info("Wiping the Neo4j database (transient per-run store; the export above is what is kept)...");
        PersistenceBridge.getHandler().deleteAllData();
    }

    /**
     * Resolves the export directory for a bare {@code run()} that did not go through {@link #runAndSave} (e.g.
     * {@code ArdocoRunner#runWithoutSaving}, which has no output directory): the {@value #CONFIGURED_EXPORT_PATH_KEY}
     * configuration key, then the {@value #CONFIGURED_EXPORT_PATH_ENV_VAR} environment variable, else
     * {@value #DEFAULT_EXPORT_DIRECTORY} in the current working directory. Always returns a directory - the wipe in
     * {@link #exportAndWipeKnowledgeGraph} must never discard the graph silently for lack of a destination.
     */
    private static File resolveConfiguredExportDirectory() {
        String configured = PersistenceBridge.getInstance().getLastAppliedConfiguration().get(CONFIGURED_EXPORT_PATH_KEY);
        if (configured == null) {
            configured = Environment.getEnv(CONFIGURED_EXPORT_PATH_ENV_VAR);
        }
        if (configured != null) {
            return new File(configured);
        }
        File defaultDirectory = new File(DEFAULT_EXPORT_DIRECTORY);
        classLogger.info("Neo4j mode: no output directory and no configured export path ({}); exporting to the default directory {}",
                CONFIGURED_EXPORT_PATH_KEY, defaultDirectory.getAbsolutePath());
        return defaultDirectory;
    }

    /**
     * Best-effort wipe after a failed run: the original failure ({@code cause}) is what gets rethrown and must
     * abort the run either way, so a failure to wipe here is logged, not rethrown (it would otherwise mask the
     * original cause). A database left behind after an aborted run is cleaned up by the next run's own start-of-run
     * wipe.
     */
    private void wipeAfterFailure(Throwable cause) {
        classLogger.error("Run failed; wiping the Neo4j database so no partial results are kept", cause);
        try {
            PersistenceBridge.getHandler().deleteAllData();
        } catch (RuntimeException wipeException) {
            classLogger.warn("Failed to wipe Neo4j after the aborted run; a stale database may remain until the next run starts", wipeException);
        }
    }

    private static void saveOutput(String name, File outputDir, ArdocoResult arDoCoResult) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(outputDir);
        Objects.requireNonNull(arDoCoResult);

        classLogger.info("Starting to write output...");
        FilePrinter.writeTraceabilityLinkRecoveryOutput(getOutputFile(name, outputDir, "traceLinks_", ".txt"), arDoCoResult);
        FilePrinter.writeTraceLinksAsCsv(arDoCoResult, outputDir);
        FilePrinter.writeInconsistencyOutput(getOutputFile(name, outputDir, "inconsistencyDetection_", ".txt"), arDoCoResult);
        classLogger.info("Finished to write output.");
    }

    private static File getOutputFile(String name, File outputDir, String prefix, String extension) {
        var filename = prefix + name + extension;
        var filepath = outputDir.toPath().resolve(filename);
        return filepath.toFile();
    }

}
