/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.util.List;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Ardocode;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.tests.task.DocumentationToModelToCodeTlrTask;
import io.github.ardoco.core.neo4jschema.Main;

import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureItemRepository;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.execution.CodeRunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Arcotl;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;

import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(classes = io.github.ardoco.core.neo4jschema.Main.class, properties = { "spring.neo4j.uri=bolt://localhost:7687",
        "spring.neo4j.authentication.username=neo4j", "spring.neo4j.authentication.password=password", "spring.data.neo4j.repositories.type=imperative",
        "spring.neo4j.pool.metrics-enabled=false" })
public class TraceLinkPersistenceTest extends CodeRunnerBaseTest {
    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5").withRandomPassword();

    @Autowired
    private ArchitectureItemRepository archRepo;

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }

    @Test
    void testArcotlPipelineWithNeo4j() throws InterruptedException {
        var runner = new Arcotl(projectName);
        ImmutableSortedMap<String, String> configs = getConfigsWithPersistence(true);
        runner.setUp(
                new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM),
                this.codeConfiguration,
                configs,
                new File(directory.toFile(), "output")
        );

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        ImmutableList<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> traceLinks = result.getSamCodeTraceLinks();
        System.out.println("Trace Links: " + traceLinks.size());

        Assertions.assertFalse(traceLinks.isEmpty());
        Assertions.assertEquals(164,traceLinks.size());
    }

    @Test
    void testArcotlPipelineWithoutPersistence() throws InterruptedException {
        var runner = new Arcotl(projectName);
        ImmutableSortedMap<String, String> configs = getConfigsWithPersistence(false);
        runner.setUp(
                new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM),
                this.codeConfiguration,
                configs,
                new File(directory.toFile(), "output")
        );

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        ImmutableList<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> traceLinks = result.getSamCodeTraceLinks();
        System.out.println("Trace Links: " + traceLinks.size());

        Assertions.assertFalse(traceLinks.isEmpty());
        Assertions.assertEquals(164,traceLinks.size());
    }

    @Test
    void testTransArcPipelineWithNeo4j() throws InterruptedException {
        var runner = new Transarc(projectName);
        ImmutableSortedMap<String, String> configs = getConfigsWithPersistence(true);
        runner.setUp(new File(inputText), new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), codeConfiguration,
                configs, new File(outputDir));

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);

        ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks = result.getSadCodeTraceLinks();
        Assertions.assertFalse(traceLinks.isEmpty());

        ImmutableList<TransitiveTraceLink<?,?>> realTransitiveTraceLinks = traceLinks.select(link -> link instanceof TransitiveTraceLink<?, ?>).collect(link -> (TransitiveTraceLink<?, ?>) link);
        Assertions.assertEquals(501, realTransitiveTraceLinks.size()); //  expected number of tracelinks taken from https://tv.ardoco.de/ which still uses Ardoco without the neo4j persistence, but with the same configuration. (as of 10.03.2026)
    }

    void testTransArcPipelineWithoutPersistence() throws InterruptedException {
        var runner = new Transarc(projectName);
        ImmutableSortedMap<String, String> configs = getConfigsWithPersistence(false);
        runner.setUp(new File(inputText), new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), codeConfiguration,
                configs, new File(outputDir));

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);

        ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks = result.getSadCodeTraceLinks();
        Assertions.assertFalse(traceLinks.isEmpty());

        ImmutableList<TransitiveTraceLink<?,?>> realTransitiveTraceLinks = traceLinks.select(link -> link instanceof TransitiveTraceLink<?, ?>).collect(link -> (TransitiveTraceLink<?, ?>) link);
        Assertions.assertEquals(501, realTransitiveTraceLinks.size()); //  expected number of tracelinks taken from https://tv.ardoco.de/ which still uses Ardoco without the neo4j persistence, but with the same configuration. (as of 10.03.2026)
    }

    @Test
    void testSwattrPipelineWithNeo4j() throws InterruptedException {
        var runner = new Swattr(projectName);
        ImmutableSortedMap<String, String> configs = getConfigsWithPersistence(true);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM),
                configs, outputDir);

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);

        ImmutableList<TraceLink<SentenceEntity, ModelEntity>> links = result.getArchitectureTraceLinks();
        System.out.println("Architecture Trace Links: " + links.size());
        Assertions.assertEquals(20, links.size());
    }

    @Test
    void testSwattrPipelineWithoutPersistence() throws InterruptedException {
        var runner = new Swattr(projectName);
        ImmutableSortedMap<String, String> configs = getConfigsWithPersistence(false);
        runner.setUp(inputText, new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM),
                configs, outputDir);

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);

        ImmutableList<TraceLink<SentenceEntity, ModelEntity>> links = result.getArchitectureTraceLinks();
        System.out.println("Architecture Trace Links: " + links.size());
        Assertions.assertEquals(20, links.size());
    }

    @Test
    void testArDoCodePipelineWithNeo4j() throws InterruptedException {
        var runner = new Ardocode(projectName);
        ImmutableSortedMap<String, String> configs = getConfigsWithPersistence(true);
        runner.setUp(new File(inputText), codeConfiguration, configs, new File(outputDir));

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> links = result.getSadCodeTraceLinks();

        System.out.println("Trace Links: " + links.size());

        Assertions.assertFalse(links.isEmpty());
        Assertions.assertEquals(2674, links.size());
    }

    @Test
    void testArDoCodePipelineWithoutPersistence() throws InterruptedException {
        var runner = new Ardocode(projectName);
        ImmutableSortedMap<String, String> configs = getConfigsWithPersistence(false);
        runner.setUp(new File(inputText), codeConfiguration, configs, new File(outputDir));

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        ImmutableList<TraceLink<SentenceEntity, ? extends ModelEntity>> links = result.getSadCodeTraceLinks();

        System.out.println("Trace Links: " + links.size());

        Assertions.assertFalse(links.isEmpty());
        Assertions.assertEquals(2674, links.size());
    }

    private ImmutableSortedMap<String, String> getConfigsWithPersistence(boolean enabled) {
        ImmutableSortedMap<String, String> configs = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        MutableSortedMap<String, String> additionalConfigs = SortedMaps.mutable.empty();
        additionalConfigs.putAll(configs.toSortedMap());
        additionalConfigs.put("PersistenceBridge::usePersistence", String.valueOf(enabled));
        return additionalConfigs.toImmutable();
    }
}
