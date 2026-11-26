/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

@SpringBootTest(classes = Main.class)
class IndexBootstrapTest extends AbstractNeo4jTest {

    private static final Set<String> PRODUCTION_INDEXES = Set.of("sentence_num_idx", "word_position_idx", "nounmapping_id_idx", "recommendedinstance_id_idx",
            "phrase_id_idx", "traceable_id_idx", "inconsistency_reason_idx", "inconsistency_id_idx");

    @Autowired
    private Neo4jClient neo4jClient;

    @Test
    @DisplayName("A neo4j-schema test context has every production index, and all of them are online")
    void productionIndexesExistAndAreOnline() {
        Set<String> online = new TreeSet<>();
        Set<String> all = new TreeSet<>();
        for (Map<String, Object> row : neo4jClient.query("SHOW INDEXES YIELD name, state RETURN name, state").fetch().all()) {
            all.add((String) row.get("name"));
            if ("ONLINE".equals(row.get("state"))) {
                online.add((String) row.get("name"));
            }
        }
        Assertions.assertTrue(all.containsAll(PRODUCTION_INDEXES), "indexes present: " + all);
        Assertions.assertTrue(online.containsAll(PRODUCTION_INDEXES), "indexes online: " + online);
    }

    @Test
    @DisplayName("A failed index creation fails the initializer instead of only logging it")
    void failedIndexCreationFails() {
        Neo4jClient failingClient = mock(Neo4jClient.class);
        when(failingClient.query(anyString())).thenThrow(new IllegalStateException("index creation refused"));

        Neo4jInitializer initializer = new Neo4jInitializer(failingClient);
        Assertions.assertThrows(RuntimeException.class, initializer::initializeNeo4j);
    }
}
