package io.github.ardoco.core;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import io.github.ardoco.core.service.architectureModel.ArchitecturePersistenceService;

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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class ArchitecturePersistenceTest {

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

        // Create Model Container
        List<ArchitectureItem> items = new ArrayList<>();
        items.add(authComponent);
        items.add(authInterface);

        var originalModel = new ArchitectureModelWithComponentsAndInterfaces(items);

        // 2. Save
        persistenceService.saveArchitectureModel(originalModel);

        // 3. Load
        var loadedModel = persistenceService.loadArchitectureModel(originalModel.getId());

        // 4. Assert
        assertThat(loadedModel).isNotNull();
        assertThat(loadedModel.getContent()).hasSize(2); // Component + Interface

        // Check Identity
        assertThat(loadedModel.getId()).isEqualTo(originalModel.getId());

        // Check Deep Structure
        ArchitectureComponent loadedComp = (ArchitectureComponent) loadedModel.getContent().stream()
                .filter(i -> i instanceof ArchitectureComponent).findFirst().orElseThrow();

        assertThat(loadedComp.getName()).isEqualTo("AuthService");
        assertThat(loadedComp.getProvidedInterfaces()).hasSize(1);

        ArchitectureInterface loadedIface = loadedComp.getProvidedInterfaces().first();
        assertThat(loadedIface.getName()).isEqualTo("IAuth");
        assertThat(loadedIface.getMethodSignatures()).hasSize(2);
    }
}
