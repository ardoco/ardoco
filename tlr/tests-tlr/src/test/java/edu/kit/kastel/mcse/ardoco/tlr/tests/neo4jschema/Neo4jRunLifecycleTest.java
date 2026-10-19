/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.JsonNode;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.common.JsonHandling;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;

/**
 * Verifies revised Phase 1 step 1.2: the Neo4j-mode run lifecycle ({@code PersistenceBridge::storage=neo4j}) - clean
 * the database at the start of a run, export the knowledge graph and wipe on success, wipe and rethrow on failure.
 * Neo4j is used like the {@link edu.kit.kastel.mcse.ardoco.core.data.DataRepository}: nothing survives a run except
 * the exported JSON file.
 */
class Neo4jRunLifecycleTest extends AbstractPersistenceTest {

    @Test
    @DisplayName("Neo4j mode cleans leftover data from a prior crashed run before the run starts")
    void cleanDatabaseAtStartRemovesLeftoverData() {
        clearNeo4jGraph();
        neo4jClient.query("CREATE (:LeftoverFromCrashedRun {marker: 'stale-from-a-crashed-run'})").run();

        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), getConfigsWithNeo4jStorage(), outputDir);
        testRunnerAssertions(runner);

        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);

        String json = readFile(knowledgeGraphFile());
        Assertions.assertFalse(json.contains("LeftoverFromCrashedRun"), "the start-of-run clean must remove data left behind by a prior crashed run");
        Assertions.assertFalse(json.contains("stale-from-a-crashed-run"));
    }

    @Test
    @DisplayName("A successful Neo4j-mode run exports the knowledge graph and wipes the database")
    void successfulRunExportsKnowledgeGraphAndWipesDatabase() {
        clearNeo4jGraph();
        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), getConfigsWithNeo4jStorage(), outputDir);
        testRunnerAssertions(runner);

        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(20, result.getArchitectureTraceLinks().size());

        Assertions.assertEquals(0, countAllNodesDirect(), "Neo4j must be wiped after a successful run; only the exported file survives");

        File exportFile = knowledgeGraphFile();
        Assertions.assertTrue(exportFile.exists(), "the knowledge graph export file must exist next to the other outputs");
        String json = readFile(exportFile);
        Assertions.assertTrue(json.contains("\"nodeCount\""));
        Assertions.assertTrue(json.contains("\"relationshipCount\""));
        Assertions.assertTrue(json.contains("\"NounMapping\""), "storage=neo4j forces persistTextState on, so NounMapping nodes must be in the export");
        Assertions.assertTrue(json.contains("\"RecommendedInstance\""), "storage=neo4j forces persistRecommendations on");
    }

    @Test
    @DisplayName("runWithoutSaving (no output directory, no configured export path) still cleans, wipes, and exports to the default directory")
    void runWithoutSavingExportsToDefaultDirectoryAndWipes() {
        clearNeo4jGraph();
        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), getConfigsWithNeo4jStorage(), outputDir);
        testRunnerAssertions(runner);

        File defaultExportFile = new File("knowledge-graph", "knowledgeGraph_" + projectName + ".json");
        try {
            DataRepository dataRepository = runner.runWithoutSaving();
            Assertions.assertNotNull(dataRepository);

            Assertions.assertEquals(0, countAllNodesDirect(), "runWithoutSaving must also clean/wipe (the lifecycle lives in Ardoco.run(), not only "
                    + "runAndSave)");
            Assertions.assertTrue(defaultExportFile.exists(),
                    "with no output directory and no configured export path, the graph must still be exported to the default directory, "
                            + "never silently discarded");
            String json = readFile(defaultExportFile);
            Assertions.assertTrue(json.contains("\"nodeCount\""));
        } finally {
            defaultExportFile.delete();
            defaultExportFile.getParentFile().delete();
        }
    }

    @Test
    @DisplayName("runWithoutSaving exports to a configured path when one is set, even with no output directory")
    void runWithoutSavingExportsToConfiguredPath(@TempDir File configuredExportDirectory) {
        clearNeo4jGraph();
        var mutableConfigs = SortedMaps.mutable.<String, String>empty();
        mutableConfigs.putAll(getConfigsWithNeo4jStorage().toSortedMap());
        mutableConfigs.put("PersistenceBridge::knowledgeGraphExportPath", configuredExportDirectory.getAbsolutePath());
        ImmutableSortedMap<String, String> configsWithExportPath = mutableConfigs.toImmutable();

        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), configsWithExportPath, outputDir);
        testRunnerAssertions(runner);

        DataRepository dataRepository = runner.runWithoutSaving();
        Assertions.assertNotNull(dataRepository);

        File exportFile = new File(configuredExportDirectory, "knowledgeGraph_" + projectName + ".json");
        Assertions.assertTrue(exportFile.exists(), "runWithoutSaving must export to the configured path when no output directory is known");
        String json = readFile(exportFile);
        Assertions.assertTrue(json.contains("\"nodeCount\""));
        Assertions.assertEquals(0, countAllNodesDirect(), "the database must still be wiped after the export");
    }

    @Test
    @DisplayName("A failed Neo4j-mode run wipes the database and still rethrows the original exception")
    void failedRunWipesDatabaseAndRethrows() {
        clearNeo4jGraph();
        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), getConfigsWithNeo4jStorage(), outputDir);
        testRunnerAssertions(runner);

        PersistenceHandler realHandler = PersistenceBridge.getHandler();
        Assertions.assertNotNull(realHandler, "storage=neo4j must have a handler available (registered by the Spring test context)");
        // Since 1.3a, NounMapping saves are deferred per stage and flushed as one saveNounMappings(Collection) batch at the end of
        // TextExtraction, not one saveNounMapping(NounMapping) call per mapping during the stage.
        PersistenceBridge.setHandler(failingOn(realHandler, "saveNounMappings"));
        try {
            RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, runner::run);
            Assertions.assertEquals("injected failure in saveNounMappings for Neo4jRunLifecycleTest", thrown.getMessage(),
                    "the original failure must propagate unmasked, not be swallowed by the wipe");
        } finally {
            PersistenceBridge.setHandler(realHandler);
        }

        Assertions.assertEquals(0, countAllNodesDirect(),
                "a failed run must leave the database wiped (no partial results survive, matching the DataRepository-only failsafe)");
    }

    @Test
    @DisplayName("Run metadata captures the filtered configuration and never leaks a secret-like value")
    void runMetadataCapturesFilteredConfigurationAndNeverLeaksSecrets() {
        clearNeo4jGraph();
        String secret = "s3cr3t-run-metadata-test";
        var mutableConfigs = SortedMaps.mutable.<String, String>empty();
        mutableConfigs.putAll(getConfigsWithNeo4jStorage().toSortedMap());
        mutableConfigs.put("FakeExternalService::password", secret);
        mutableConfigs.put("FakeExternalService::connectionUri", "bolt://user:" + secret + "@127.0.0.1:7687");
        ImmutableSortedMap<String, String> configsWithSecret = mutableConfigs.toImmutable();

        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), configsWithSecret, outputDir);
        testRunnerAssertions(runner);

        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        rootLogger.addAppender(appender);
        ArdocoResult result;
        try {
            result = runner.run();
        } finally {
            rootLogger.detachAppender(appender);
        }
        Assertions.assertNotNull(result);

        boolean secretInLogs = appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains(secret));
        Assertions.assertFalse(secretInLogs, "the secret value must never be logged");

        String json = readFile(knowledgeGraphFile());
        Assertions.assertFalse(json.contains(secret), "the secret value must never appear anywhere in the exported knowledge graph");
        Assertions.assertFalse(json.contains("FakeExternalService::password"), "a key matching the secret filter must be dropped entirely");
        Assertions.assertTrue(json.contains("PersistenceBridge::storage"));
        Assertions.assertTrue(json.contains("bolt://***@127.0.0.1:7687"), "a surviving key's URI value must still have its user-info masked");

        List<JsonNode> runMetadataNodes = nodesWithLabel(json, "RunMetadata");
        Assertions.assertEquals(1, runMetadataNodes.size(), "exactly one RunMetadata node must exist");
        JsonNode properties = runMetadataNodes.get(0).get("properties");
        Assertions.assertEquals(projectName, properties.get("projectName").asText());
        Instant.parse(properties.get("startTimestamp").asText());
        Instant.parse(properties.get("finishTimestamp").asText());
    }

    private static List<JsonNode> nodesWithLabel(String exportedJson, String label) {
        List<JsonNode> matches = new ArrayList<>();
        try {
            JsonNode root = JsonHandling.createObjectMapper().readTree(exportedJson);
            for (JsonNode node : root.get("nodes")) {
                for (JsonNode nodeLabel : node.get("labels")) {
                    if (nodeLabel.asText().equals(label)) {
                        matches.add(node);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return matches;
    }

    private File knowledgeGraphFile() {
        return new File(outputDir, "knowledgeGraph_" + projectName + ".json");
    }

    private long countAllNodesDirect() {
        return neo4jClient.query("MATCH (n) RETURN count(n) AS c").fetch().one().map(row -> ((Number) row.get("c")).longValue()).orElse(0L);
    }

    private static String readFile(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Wraps {@code real} so the given method throws instead of delegating; every other call is forwarded unchanged.
     * Used to inject a deterministic mid-run failure (well after earlier stages already wrote real data), without
     * hand-writing a full {@link PersistenceHandler} stub for every interface method.
     */
    private static PersistenceHandler failingOn(PersistenceHandler real, String failingMethodName) {
        return (PersistenceHandler) Proxy.newProxyInstance(PersistenceHandler.class.getClassLoader(), new Class<?>[] { PersistenceHandler.class },
                (proxy, method, args) -> {
                    if (method.getName().equals(failingMethodName)) {
                        throw new RuntimeException("injected failure in " + failingMethodName + " for Neo4jRunLifecycleTest");
                    }
                    try {
                        return method.invoke(real, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
