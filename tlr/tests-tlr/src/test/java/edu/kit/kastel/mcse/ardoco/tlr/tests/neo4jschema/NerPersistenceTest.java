/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.TreeSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.api.text.PlainSimpleText;
import edu.kit.kastel.mcse.ardoco.core.api.SimplePreprocessingData;
import io.github.ardoco.core.neo4jschema.Neo4jPersistenceHandler;

/**
 * Unit-style Neo4j Desktop tests for NER dual-write / load APIs (Phase 3).
 * Full Artemis LLM pipeline tests are optional and flag-gated separately.
 */
class NerPersistenceTest extends AbstractPersistenceTest {

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Test
    @DisplayName("NER entity save and load round-trip")
    void testNerEntityRoundTrip() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance()
                .applyConfiguration(org.eclipse.collections.api.factory.SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true",
                        "PersistenceBridge::persistNerConnection", "true"));

        var occurrence = new NamedArchitectureEntityOccurrence("WebUI", 2);
        var entity = new NamedArchitectureEntity("WebUI", new TreeSet<>(List.of("UI")), List.of(occurrence));
        Metamodel mm = Metamodel.ARCHITECTURE_WITH_COMPONENTS;

        persistenceHandler.saveNamedArchitectureEntity(entity, mm, false);
        assertThat(persistenceHandler.hasNerNamedArchitectureEntities()).isTrue();

        var loaded = persistenceHandler.loadNamedArchitectureEntities(mm, false);
        assertThat(loaded).hasSize(1);
        NamedArchitectureEntity restored = loaded.iterator().next();
        assertThat(restored.getId()).isEqualTo(entity.getId());
        assertThat(restored.getName()).isEqualTo("WebUI");
        assertThat(restored.getOccurrences()).hasSize(1);
        assertThat(restored.getOccurrences().get(0).getSentenceNumber()).isEqualTo(2);

        // Also verify SimpleText coexists (NER depends on it for resume paths)
        persistenceHandler.saveSimpleText(new PlainSimpleText("line1\nline2", List.of("line1", "line2")), SimplePreprocessingData.ID);
        assertThat(persistenceHandler.hasSimpleText(SimplePreprocessingData.ID)).isTrue();

        pauseForNeo4jInspection();
    }
}
