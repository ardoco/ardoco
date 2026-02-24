/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;

import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

import io.github.ardoco.core.neo4jschema.Main;

import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureItemRepository;

import org.eclipse.collections.api.list.ImmutableList;
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

    @BeforeEach
    void cleanDatabase() {
        archRepo.deleteAll();
    }

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }

    @Test
    void testArcotlPipelineWithNeo4j() throws InterruptedException {
        var runner = new Arcotl(projectName);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), this.codeConfiguration, additionalConfigsMap, new File(
                directory.toFile(), "output"));

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        ImmutableList<TraceLink<? extends ArchitectureEntity, ? extends ModelEntity>> traceLinks = result.getSamCodeTraceLinks();
        System.out.println("Trace Links: " + traceLinks.size());
        // print tracelinks


        Assertions.assertFalse(traceLinks.isEmpty());
        Assertions.assertEquals(164,traceLinks.size());

    }
}
