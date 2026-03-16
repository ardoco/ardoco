/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import static edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.ArchitectureModelEqualityHelper.assertArchitectureModelsEqual;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.uml.UmlExtractor;
import io.github.ardoco.core.neo4jschema.service.architectureModel.ArchitecturePersistenceService;

@Transactional
class ArchitecturePersistenceTest extends AbstractPersistenceTest {

    @Autowired
    private ArchitecturePersistenceService persistenceService;

    @Test
    @DisplayName("Should persist and restore manual dummy Architecture Model")
    void testSaveAndLoadManualArchitecture() {

        var method1 = new ArchitectureMethod("login");
        var method2 = new ArchitectureMethod("logout");
        var methods = new TreeSet<>(List.of(method1, method2));

        var authInterface = new ArchitectureInterface("IAuth", "id_interface_1", methods);

        var providedInterfaces = new TreeSet<>(List.of(authInterface));
        var requiredInterfaces = new TreeSet<ArchitectureInterface>(); // Empty
        var subcomponents = new TreeSet<ArchitectureComponent>();      // Empty

        var authComponent = new ArchitectureComponent("AuthService", "id_comp_1", subcomponents, providedInterfaces, requiredInterfaces, "Service");
        List<ArchitectureItem> items = new ArrayList<>();
        items.add(authComponent);
        items.add(authInterface);

        var originalModel = new ArchitectureModelWithComponentsAndInterfaces(items);
        persistenceService.saveArchitectureModel(originalModel);
        var loadedModel = persistenceService.loadArchitectureModel(originalModel.getMetamodel());

        assertArchitectureModelsEqual(originalModel, loadedModel);

        Assertions.assertThat(loadedModel).isNotNull();
        Assertions.assertThat(loadedModel.getContent()).hasSize(2); // Component + Interface
        Assertions.assertThat(loadedModel.getId()).isEqualTo(originalModel.getId());
        ArchitectureComponent loadedComp = (ArchitectureComponent) loadedModel.getContent()
                .stream()
                .filter(i -> i instanceof ArchitectureComponent)
                .findFirst()
                .orElseThrow();

        assertThat(loadedComp.getName()).isEqualTo("AuthService");
        assertThat(loadedComp.getProvidedInterfaces()).hasSize(1);

        ArchitectureInterface loadedIface = loadedComp.getProvidedInterfaces().first();
        assertThat(loadedIface.getName()).isEqualTo("IAuth");
        assertThat(loadedIface.getMethodSignatures()).hasSize(2);
    }

    @Test
    @DisplayName("Should persist and restore a UML-extracted Architecture Model")
    void testSaveAndLoadUmlArchitecture() {
        UmlExtractor extractor = new UmlExtractor(this.inputModelArchitectureUml, Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES);
        ArchitectureModel originalModel = extractor.extractModel();

        persistenceService.saveArchitectureModel(originalModel);
        ArchitectureModel loadedModel = persistenceService.loadArchitectureModel(originalModel.getMetamodel());

        assertThat(loadedModel).isNotNull();
        assertArchitectureModelsEqual(originalModel, loadedModel);
    }
}
