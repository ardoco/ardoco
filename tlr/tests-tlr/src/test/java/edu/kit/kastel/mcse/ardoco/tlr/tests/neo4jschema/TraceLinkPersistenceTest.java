/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Arcotl;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Ardocode;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;


public class TraceLinkPersistenceTest extends AbstractPersistenceTest {

    @Test
    @DisplayName("Test Arcotl pipeline with Neo4j persistence")
    void testArcotlPipelineWithNeo4j() {
        runAndAssertArcotl(true);
    }

    @Test
    @DisplayName("Test Arcotl pipeline without persistence")
    void testArcotlPipelineWithoutPersistence() {
        runAndAssertArcotl(false);
    }

    private void runAndAssertArcotl(boolean persistence) {
        var runner = new Arcotl(projectName);
        runner.setUp(new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), this.codeConfiguration,
                getConfigsWithPersistence(persistence), new File(directory.toFile(), "output"));

        testRunnerAssertions(runner);
        var result = runner.run();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(164, result.getSamCodeTraceLinks().size());
    }

    private void runAndAssertTransarc(boolean persistence) {
        var runner = new Transarc(projectName);
        runner.setUp(new File(inputText), new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), codeConfiguration,
                getConfigsWithPersistence(persistence), new File(outputDir));

        testRunnerAssertions(runner);
        var result = runner.run();
        Assertions.assertNotNull(result);

        ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks = result.getSadCodeTraceLinks();
        Assertions.assertFalse(traceLinks.isEmpty());

        int transitiveCount = traceLinks.select(l -> l instanceof TransitiveTraceLink<?, ?>).size();
        Assertions.assertEquals(501, transitiveCount);
    }

    @Test
    @DisplayName("Test TransArc pipeline with Neo4j persistence")
    void testTransArcPipelineWithNeo4j() {
        runAndAssertTransarc(true);
    }

    @Test
    @DisplayName("Test TransArc pipeline without persistence")
    void testTransArcPipelineWithoutPersistence() {
        runAndAssertTransarc(false);
    }

    private void runAndAssertSwattr(boolean persistence) {
        var runner = new Swattr(projectName);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), getConfigsWithPersistence(persistence),
                outputDir);

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);

        int linkCount = result.getArchitectureTraceLinks().size();
        Assertions.assertEquals(20, linkCount);
    }

    @Test
    @DisplayName("Test SWATTR pipeline with Neo4j persistence")
    void testSwattrPipelineWithNeo4j() {
        runAndAssertSwattr(true);
    }

    @Test
    @DisplayName("Test SWATTR pipeline without persistence")
    void testSwattrPipelineWithoutPersistence() {
        runAndAssertSwattr(false);
    }

    private void runAndAssertArdocode(boolean persistence) {
        var runner = new Ardocode(projectName);
        runner.setUp(new File(inputText), codeConfiguration, getConfigsWithPersistence(persistence), new File(outputDir));

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> links = result.getSadCodeTraceLinks();

        Assertions.assertFalse(links.isEmpty());
        Assertions.assertEquals(2674, links.size());
    }

    @Test
    @DisplayName("Test ARDoCo pipeline with Neo4j persistence")
    void testArDoCodePipelineWithNeo4j() {
        runAndAssertArdocode(true);
    }

    @Test
    @DisplayName("Test ARDoCo pipeline without persistence")
    void testArDoCodePipelineWithoutPersistence() {
        runAndAssertArdocode(false);
    }
}
