/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceStorage;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;

/**
 * Two runners in one JVM share the global persistence settings of {@link PersistenceBridge}. A runner must not run with
 * the storage settings of another runner that was set up after it.
 */
class SequentialRunnersTest extends AbstractPersistenceTest {

    private static final String MARKER_KEY = "SequentialRunnersTest::marker";
    private static final String EXPORT_PATH_KEY = "PersistenceBridge::knowledgeGraphExportPath";

    @Test
    @DisplayName("A neo4j runner set up before a memory runner fails fast at run() instead of running in memory mode")
    void interleavedSetUpWithDifferentStorageFailsFast(@TempDir File dirA, @TempDir File dirB) {
        clearNeo4jGraph();
        Swattr runnerA = setUpRunner(getConfigsWithNeo4jStorage(), dirA);
        Swattr runnerB = setUpRunner(getConfigsWithMemoryStorage(), dirB);
        Assertions.assertTrue(runnerB.isSetUp());

        IllegalStateException failure = Assertions.assertThrows(IllegalStateException.class, runnerA::run);

        String message = failure.getMessage();
        Assertions.assertTrue(message.contains(PersistenceStorage.NEO4J.toString()), message);
        Assertions.assertTrue(message.contains(PersistenceStorage.MEMORY.toString()), message);
        Assertions.assertFalse(message.contains(neo4jPassword()), "the message must never contain the password");
        Assertions.assertArrayEquals(new String[0], outputFiles(dirA), "the failed run must write no output file");
        Assertions.assertEquals(0, countNodes(), "the failed run must leave the database empty");
    }

    @Test
    @DisplayName("Set up and run one runner at a time: neo4j then memory both succeed")
    void normalOrderNeo4jThenMemorySucceeds(@TempDir File dirA, @TempDir File dirB) {
        clearNeo4jGraph();
        Swattr runnerA = setUpRunner(getConfigsWithNeo4jStorage(), dirA);
        Assertions.assertNotNull(runnerA.run());
        Assertions.assertTrue(new File(dirA, "knowledgeGraph_" + projectName + ".json").exists(), "the neo4j run must export its knowledge graph");
        Assertions.assertEquals(0, countNodes(), "the neo4j run must wipe the database");

        Swattr runnerB = setUpRunner(getConfigsWithMemoryStorage(), dirB);
        Assertions.assertNotNull(runnerB.run());
        Assertions.assertFalse(PersistenceBridge.isNeo4jStorage());
        Assertions.assertFalse(new File(dirB, "knowledgeGraph_" + projectName + ".json").exists(), "the memory run must not export a knowledge graph");
        Assertions.assertTrue(outputFiles(dirB).length > 0, "the memory run must write its output files");
        Assertions.assertEquals(0, countNodes(), "the database must be empty after both runs");
    }

    @Test
    @DisplayName("Two memory runners set up one after the other still run")
    void interleavedMemoryRunnersStillRun(@TempDir File dirA, @TempDir File dirB) {
        Swattr runnerA = setUpRunner(getConfigsWithMemoryStorage(), dirA);
        Swattr runnerB = setUpRunner(getConfigsWithMemoryStorage(), dirB);

        Assertions.assertNotNull(runnerA.run());
        Assertions.assertNotNull(runnerB.run());
        Assertions.assertTrue(outputFiles(dirA).length > 0);
        Assertions.assertTrue(outputFiles(dirB).length > 0);
    }

    @Test
    @DisplayName("Two neo4j runners with the same connection but another configuration: the first one fails fast, the second one runs with its own data")
    void interleavedNeo4jSetUpWithOtherConfigurationFailsFast(@TempDir File dirA, @TempDir File dirB) throws IOException {
        clearNeo4jGraph();
        Swattr runnerA = setUpRunner(getConfigsWithNeo4jStorage().newWithKeyValue(MARKER_KEY, "runner-a-marker"), dirA);
        Swattr runnerB = setUpRunner(getConfigsWithNeo4jStorage().newWithKeyValue(MARKER_KEY, "runner-b-marker"), dirB);

        IllegalStateException failure = Assertions.assertThrows(IllegalStateException.class, runnerA::run);

        assertGuardMessage(failure.getMessage());
        Assertions.assertArrayEquals(new String[0], outputFiles(dirA), "the failed run must write nothing into its own directory");
        Assertions.assertArrayEquals(new String[0], outputFiles(dirB), "the failed run must write nothing into the other runner's directory");
        Assertions.assertEquals(0, countNodes(), "the failed run must leave the database empty");

        Assertions.assertNotNull(runnerB.run());
        File exportB = new File(dirB, "knowledgeGraph_" + projectName + ".json");
        Assertions.assertTrue(exportB.exists(), "the second runner must export its knowledge graph into its own directory");
        String exportedGraph = Files.readString(exportB.toPath(), StandardCharsets.UTF_8);
        Assertions.assertTrue(exportedGraph.contains("runner-b-marker"), "the run metadata must carry the second runner's configuration");
        Assertions.assertFalse(exportedGraph.contains("runner-a-marker"), "the run metadata must not carry the first runner's configuration");
        Assertions.assertArrayEquals(new String[0], outputFiles(dirA), "the second run must write nothing into the first runner's directory");
        Assertions.assertEquals(0, countNodes(), "the second run must wipe the database");
    }

    @Test
    @DisplayName("Two neo4j runners with the same connection but another export path: runWithoutSaving of the first one fails fast")
    void interleavedNeo4jSetUpWithOtherExportPathFailsFast(@TempDir File dirA, @TempDir File dirB, @TempDir File exportA, @TempDir File exportB) {
        clearNeo4jGraph();
        Swattr runnerA = setUpRunner(getConfigsWithNeo4jStorage().newWithKeyValue(EXPORT_PATH_KEY, exportA.getAbsolutePath()), dirA);
        Swattr runnerB = setUpRunner(getConfigsWithNeo4jStorage().newWithKeyValue(EXPORT_PATH_KEY, exportB.getAbsolutePath()), dirB);
        Assertions.assertTrue(runnerB.isSetUp());

        IllegalStateException failure = Assertions.assertThrows(IllegalStateException.class, runnerA::runWithoutSaving);

        assertGuardMessage(failure.getMessage());
        Assertions.assertArrayEquals(new String[0], outputFiles(exportA), "the failed run must export nothing into its own export path");
        Assertions.assertArrayEquals(new String[0], outputFiles(exportB), "the failed run must export nothing into the other runner's export path");
        Assertions.assertEquals(0, countNodes(), "the failed run must leave the database empty");
    }

    @Test
    @DisplayName("Two neo4j runners set up with identical settings: the first one still runs")
    void identicalNeo4jSetUpsStillRun(@TempDir File dirA, @TempDir File dirB) throws IOException {
        clearNeo4jGraph();
        ImmutableSortedMap<String, String> configs = getConfigsWithNeo4jStorage().newWithKeyValue(MARKER_KEY, "same-marker");
        Swattr runnerA = setUpRunner(configs, dirA);
        Swattr runnerB = setUpRunner(configs, dirB);
        Assertions.assertTrue(runnerB.isSetUp());

        Assertions.assertNotNull(runnerA.run());

        File exportA = new File(dirA, "knowledgeGraph_" + projectName + ".json");
        Assertions.assertTrue(exportA.exists(), "the run must export its knowledge graph into its own directory");
        Assertions.assertTrue(Files.readString(exportA.toPath(), StandardCharsets.UTF_8).contains("same-marker"));
        Assertions.assertArrayEquals(new String[0], outputFiles(dirB), "the run must write nothing into the other runner's directory");
        Assertions.assertEquals(0, countNodes(), "the run must wipe the database");
    }

    private static void assertGuardMessage(String message) {
        Assertions.assertTrue(message.startsWith("The persistence settings changed after this runner was set up"), message);
        Assertions.assertFalse(message.contains(neo4jPassword()), "the message must never contain the password");
        Assertions.assertFalse(message.contains("marker"), "the message must not contain configuration values");
    }

    private Swattr setUpRunner(ImmutableSortedMap<String, String> configs, File outputDirectory) {
        var runner = new Swattr(projectName);
        runner.setUp(new File(inputText), new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), configs, outputDirectory);
        return runner;
    }

    private static String[] outputFiles(File directory) {
        String[] files = directory.list();
        return files == null ? new String[0] : files;
    }

    private long countNodes() {
        return neo4jClient.query("MATCH (n) RETURN count(n) AS c").fetch().one().map(row -> ((Number) row.get("c")).longValue()).orElse(0L);
    }
}
