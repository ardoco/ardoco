/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.kit.kastel.mcse.ardoco.core.api.SimplePreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.text.PlainSimpleText;
import edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import io.github.ardoco.core.neo4jschema.Neo4jPersistenceHandler;

/**
 * Round-trip tests for thin SimpleText Neo4j slice (Phase 2) and Project metadata (Phase 4).
 */
class SimpleTextPersistenceTest extends AbstractPersistenceTest {

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Test
    @DisplayName("SimpleText save → wipe memory → load preserves line count and content")
    void testSimpleTextRoundTrip() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance()
                .applyConfiguration(org.eclipse.collections.api.factory.SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true"));

        String content = "First line.\nSecond line.\nThird line.";
        SimpleText original = new PlainSimpleText(content, List.of("First line.", "Second line.", "Third line."));
        persistenceHandler.saveSimpleText(original, SimplePreprocessingData.ID);

        assertThat(persistenceHandler.hasSimpleText(SimplePreprocessingData.ID)).isTrue();
        SimpleText loaded = persistenceHandler.loadSimpleText(SimplePreprocessingData.ID);
        assertThat(loaded).isNotNull();
        assertThat(loaded.getText()).isEqualTo(content);
        assertThat(loaded.getLines()).hasSize(3);
        assertThat(loaded.getLines().get(1)).isEqualTo("Second line.");

        // Empty DR: sole-store / resume path via DataRepositoryHelper
        DataRepository empty = new DataRepository();
        SimpleText fromHelper = DataRepositoryHelper.getSimpleText(empty);
        assertThat(fromHelper.getText()).isEqualTo(content);
        assertThat(fromHelper.getLines()).hasSize(3);

        pauseForNeo4jInspection();
    }

    @Test
    @DisplayName("Project metadata round-trip")
    void testProjectMetadataRoundTrip() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance()
                .applyConfiguration(org.eclipse.collections.api.factory.SortedMaps.immutable.with("PersistenceBridge::usePersistence", "true"));

        persistenceHandler.saveProjectMetadata("teastore", "raw input text");
        assertThat(persistenceHandler.hasProjectMetadata("teastore")).isTrue();
        assertThat(persistenceHandler.loadProjectInputText("teastore")).isEqualTo("raw input text");
        assertThat(persistenceHandler.loadSoleProjectName()).isEqualTo("teastore");

        pauseForNeo4jInspection();
    }
}
