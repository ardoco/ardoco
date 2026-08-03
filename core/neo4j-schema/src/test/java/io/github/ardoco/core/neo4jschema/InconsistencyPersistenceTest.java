/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.ModelInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.TextInconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.ModelEntityAbsentFromTextInconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.TextEntityAbsentFromModelInconsistency;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureComponentNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;
import io.github.ardoco.core.neo4jschema.repository.TraceableNodeRepository;
import io.github.ardoco.core.neo4jschema.repository.documentation.SentenceNodeRepository;
import io.github.ardoco.core.neo4jschema.service.InconsistencyPersistenceService;
import io.github.ardoco.core.neo4jschema.util.FakeArchitectureEntity;

@SpringBootTest(classes = io.github.ardoco.core.neo4jschema.Main.class, properties = "ardoco.persistence.neo4j.enabled=true")
@Import({ Neo4jBridgeActivator.class, Neo4jInitializer.class })
class InconsistencyPersistenceTest extends AbstractNeo4jTest {

    @Autowired
    private InconsistencyPersistenceService persistenceService;

    @Autowired
    private SentenceNodeRepository sentenceNodeRepository;

    @Autowired
    private TraceableNodeRepository traceableNodeRepository;

    @Test
    void shouldPersistRelationshipAndAvoidNPE() {
        var fakeEntity = new FakeArchitectureEntity("OrderService", "COMP_123", "Component");

        ArchitectureComponentNode archNode = new ArchitectureComponentNode("OrderService", "Component", "COMP_123");
        traceableNodeRepository.save(archNode);

        ModelInconsistency inconsistency = new ModelEntityAbsentFromTextInconsistency(fakeEntity);
        persistenceService.addInconsistencies(List.of(inconsistency));

        var results = persistenceService.getInconsistencies();

        assertFalse(results.isEmpty(), "Should have retrieved an inconsistency");
        Inconsistency retrieved = results.iterator().next();

        assertTrue(retrieved instanceof ModelEntityAbsentFromTextInconsistency, "Retrieved inconsistency should be a ModelInconsistency");
        ModelEntityAbsentFromTextInconsistency entity = (ModelEntityAbsentFromTextInconsistency) retrieved;

        assertEquals("COMP_123", entity.getModelInstanceUid());
        assertTrue(retrieved.getReason().contains("OrderService"));
    }

    @Test
    void shouldPersistTextInconsistencyAndRetrieveCorrectly() {
        int sentenceNum = 5;
        String sentenceText = "The system shall provide a Login component.";
        SentenceNode sentenceNode = new SentenceNode(sentenceNum, sentenceText);
        sentenceNodeRepository.save(sentenceNode);

        String name = "inconsistency";
        double confidence = 0.85;
        TextInconsistency textInconsistency = new TextEntityAbsentFromModelInconsistency(name, sentenceNum, confidence, null);

        persistenceService.addInconsistencies(List.of(textInconsistency));
        var results = persistenceService.getInconsistencies();

        assertFalse(results.isEmpty(), "Should have retrieved an inconsistency");

        TextInconsistency retrieved = results.stream().filter(i -> i instanceof TextInconsistency).map(i -> (TextInconsistency) i).findFirst().orElseThrow();

        assertEquals(sentenceNum, retrieved.getSentenceNumber());
        assertTrue(retrieved.getReason().contains(name));
        assertTrue(retrieved.getReason().contains("0.85"));
        assertEquals("TextEntityAbsentFromModel", retrieved.getType());
    }
}
