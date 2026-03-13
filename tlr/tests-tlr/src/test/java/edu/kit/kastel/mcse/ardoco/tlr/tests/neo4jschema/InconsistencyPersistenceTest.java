package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.InconsistentSentence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.TextInconsistency;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.RunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.id.execution.runner.InconsistencyDetection;
import io.github.ardoco.core.neo4jschema.Main;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

@Testcontainers
@SpringBootTest(classes = io.github.ardoco.core.neo4jschema.Main.class, properties = { "spring.neo4j.uri=bolt://localhost:7687",
        "spring.neo4j.authentication.username=neo4j", "spring.neo4j.authentication.password=password", "spring.data.neo4j.repositories.type=imperative",
        "spring.neo4j.pool.metrics-enabled=false" })
public class InconsistencyPersistenceTest  extends RunnerBaseTest {

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5").withRandomPassword();

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }

    @Test
    void testInconsistencyDetectionWithNeo4j() {
        var runner = new InconsistencyDetection(projectName);
        File additionalConfigsFile = new File(additionalConfigs);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(additionalConfigsFile);
        runner.setUp(inputText, inputModelArchitecture, ModelFormat.PCM, additionalConfigsMap, outputDir);

        testRunnerAssertions(runner);
        ArdocoResult result = runner.run();
        Assertions.assertNotNull(result);
        ImmutableList<Inconsistency> inconsistencies = result.getAllInconsistencies();
        System.out.println("Found a total of " + inconsistencies.size() + " inconsistencies."); // 9
        ImmutableList<TextInconsistency> textInconsistencies = result.getAllTextInconsistencies();
        System.out.println("Found a total of " + textInconsistencies.size() + " text inconsistencies."); // 4 vs 24 on traceview


        ImmutableList<ModelInconsistency> modelInconsistencies = result.getAllModelInconsistencies();
        Assertions.assertEquals(5, modelInconsistencies.size());
        System.out.println("Found a total of " + modelInconsistencies.size() + " model inconsistencies."); // 5


        ImmutableList<InconsistentSentence> inconsistentSentences = result.getInconsistentSentences();
        System.out.println("Found a total of " + inconsistentSentences.size() + " inconsistent sentences."); //4
    }

}
