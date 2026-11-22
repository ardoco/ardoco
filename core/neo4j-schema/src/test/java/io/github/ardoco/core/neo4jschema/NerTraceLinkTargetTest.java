/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.List;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import io.github.ardoco.core.neo4jschema.util.FakeArchitectureEntity;

/**
 * A named-entity trace link whose target is a model entity that is neither an architecture item, a code item nor a code file cannot be typed: saving
 * it throws an {@link IllegalStateException} that names the target class, and no trace link is written.
 */
@SpringBootTest(classes = Main.class)
class NerTraceLinkTargetTest extends AbstractNeo4jTest {

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void clearDatabase() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    @DisplayName("A named-entity trace link to an unknown model entity kind throws IllegalStateException naming the target class; nothing is written")
    void unknownNerTargetFails() {
        var occurrence = new NamedArchitectureEntityOccurrence("Gateway", 1);
        var entity = new NamedArchitectureEntity("Gateway", new TreeSet<>(), List.of(occurrence));
        persistenceHandler.saveNamedArchitectureEntity(entity, new ArtemisTarget(Metamodel.ARCHITECTURE_WITH_COMPONENTS, NamedEntityType.COMPONENT), false);
        var target = new FakeArchitectureEntity("Gateway", "ner_unknown_target", "component");
        var link = new NamedArchitectureEntityToModelTraceLink(occurrence, target);

        IllegalStateException thrown = Assertions.assertThrows(IllegalStateException.class, () -> persistenceHandler.saveTraceLinks(List.of(link)));

        Assertions.assertTrue(thrown.getMessage().contains(FakeArchitectureEntity.class.getName()), thrown.getMessage());
        Assertions.assertEquals(0L, neo4jClient.query("MATCH ()-[r:TRACES_TO]->() RETURN count(r) AS c").fetchAs(Long.class).one().orElseThrow());
    }
}
