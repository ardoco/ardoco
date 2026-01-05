package io.github.ardoco.core;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.execution.CodeRunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.core.execution.ConfigurationHelper;
import edu.kit.kastel.mcse.ardoco.core.execution.RunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Ardocode;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.code.CodeExtractor;
import io.github.ardoco.core.repository.codeModel.CodeModelRepository;
import io.github.ardoco.core.service.architectureModel.ArchitecturePersistenceService;

import io.github.ardoco.core.service.codeModel.CodePersistenceService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

import static io.github.ardoco.core.util.CodeModelEqualityHelper.areCodeModelsEqual;

@Testcontainers
@SpringBootTest
@Transactional
public class CodePersistenceTest extends CodeRunnerBaseTest {

    // TODO run pipeline ardocode as a test and see if it works.

    @Autowired
    private CodePersistenceService persistenceService;

    @Autowired
    private CodeModelRepository codeModelRepository;

    @Container
    static Neo4jContainer<?> neo4j = new Neo4jContainer<>("neo4j:5.15.0")
            .withRandomPassword();

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4j::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", neo4j::getAdminPassword);
    }

    @Test
    @DisplayName("Should persist and restore a Code Model")
    void testSaveAndLoadCodeModel() {
        File codeFile = codeConfiguration.code();
        Metamodel model = codeConfiguration.metamodel();

        CodeModel extractedModel = CodeExtractor.readInCodeModel(codeFile, Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES);
        persistenceService.saveCodeModel(extractedModel);
        CodeModel loadedModel = persistenceService.loadCodeModel(extractedModel.getId());
        Assertions.assertTrue(areCodeModelsEqual(extractedModel, loadedModel));
    }

    @Test
    @DisplayName("Should run ArdoCode pipeline and persist results")
    void testArdoCodePipelineWithNeo4j() {
        var runner = new Ardocode(projectName);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(new File(additionalConfigs));
        runner.setUp(new File(inputText), codeConfiguration, additionalConfigsMap, new File(outputDir));

        testRunnerAssertions(runner);
        Assertions.assertNotNull(runner.run());
    }
}
