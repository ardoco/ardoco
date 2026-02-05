package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.execution.RunnerBaseTest;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.UmlExtractor;
import io.github.ardoco.core.neo4jschema.Main;
import io.github.ardoco.core.neo4jschema.service.architectureModel.ArchitecturePersistenceService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.ArchitectureModelEqualityHelper.assertArchitectureModelsEqual;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(
        classes = io.github.ardoco.core.neo4jschema.Main.class,
        properties = {
                "spring.neo4j.uri=bolt://localhost:7687",
                "spring.neo4j.authentication.username=neo4j",
                "spring.neo4j.authentication.password=password",
                "spring.data.neo4j.repositories.type=imperative",
                "spring.neo4j.pool.metrics-enabled=false"
        }
)
@Transactional
class ArchitecturePersistenceTest extends RunnerBaseTest {

    @Autowired
    private ArchitecturePersistenceService persistenceService;

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
    @DisplayName("Should persist and restore an Architecture Model")
    void testSaveAndLoadArchitecture() {
        // 1. Create Dummy Data
        var method1 = new ArchitectureMethod("login");
        var method2 = new ArchitectureMethod("logout");
        var methods = new TreeSet<>(List.of(method1, method2));

        var authInterface = new ArchitectureInterface("IAuth", "id_interface_1", methods);

        var providedInterfaces = new TreeSet<>(List.of(authInterface));
        var requiredInterfaces = new TreeSet<ArchitectureInterface>(); // Empty
        var subcomponents = new TreeSet<ArchitectureComponent>();      // Empty

        var authComponent = new ArchitectureComponent(
                "AuthService", "id_comp_1",
                subcomponents, providedInterfaces, requiredInterfaces, "Service"
        );
        List<ArchitectureItem> items = new ArrayList<>();
        items.add(authComponent);
        items.add(authInterface);

        var originalModel = new ArchitectureModelWithComponentsAndInterfaces(items);
        persistenceService.saveArchitectureModel(originalModel);
        var loadedModel = persistenceService.loadArchitectureModel(originalModel.getMetamodel());


        Assertions.assertThat(loadedModel).isNotNull();
        Assertions.assertThat(loadedModel.getContent()).hasSize(2); // Component + Interface
        Assertions.assertThat(loadedModel.getId()).isEqualTo(originalModel.getId());
        ArchitectureComponent loadedComp = (ArchitectureComponent) loadedModel.getContent().stream()
                .filter(i -> i instanceof ArchitectureComponent).findFirst().orElseThrow();

        assertThat(loadedComp.getName()).isEqualTo("AuthService");
        assertThat(loadedComp.getProvidedInterfaces()).hasSize(1);

        ArchitectureInterface loadedIface = loadedComp.getProvidedInterfaces().first();
        assertThat(loadedIface.getName()).isEqualTo("IAuth");
        assertThat(loadedIface.getMethodSignatures()).hasSize(2);
    }

    @Test
    void testSaveAndLoadCodeModel2() {
        UmlExtractor extractor = new UmlExtractor(this.inputModelArchitectureUml, Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES);
        ArchitectureModel originalModel = extractor.extractModel();
        persistenceService.saveArchitectureModel(originalModel);
        ArchitectureModel loadedModel = persistenceService.loadArchitectureModel(originalModel.getMetamodel());
        System.out.println("Hello from testSaveAndLoadCodeModel2");
        assertThat(loadedModel).isNotNull();
        assertArchitectureModelsEqual(originalModel, loadedModel);
    }
}
