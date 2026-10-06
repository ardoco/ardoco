/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import io.github.ardoco.core.neo4jschema.Neo4jPersistenceHandler;

/**
 * Merge in TextState removes the loser mapping in memory and calls {@code deleteNounMapping}.
 * This verifies the Neo4j loser node is removed after that delete.
 */
class NounMappingMergePersistenceTest extends AbstractPersistenceTest {

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Test
    @DisplayName("deleteNounMapping removes merged-away NounMapping node from Neo4j")
    void testDeleteNounMappingRemovesLoserAfterMerge() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance().applyConfiguration(getConfigsWithPersistence(true, true, false));

        String winnerId = "nm-winner-merge-test";
        String loserId = "nm-loser-merge-test";
        createBareNounMappingNode(winnerId);
        createBareNounMappingNode(loserId);
        Assertions.assertEquals(2, countNodesWithLabel("NounMapping"));

        persistenceHandler.deleteNounMapping(loserId);

        Assertions.assertEquals(1, countNodesWithLabel("NounMapping"));
        Assertions.assertEquals(0, countNounMappingByArdocoId(loserId));
        Assertions.assertEquals(1, countNounMappingByArdocoId(winnerId));

        pauseForNeo4jInspection();
    }

    private void createBareNounMappingNode(String ardocoId) {
        neo4jClient.query("CREATE (n:NounMapping {ardocoId: $id})")
                .bind(ardocoId)
                .to("id")
                .run();
    }

    private long countNounMappingByArdocoId(String ardocoId) {
        return neo4jClient.query("MATCH (n:NounMapping {ardocoId: $id}) RETURN count(n) AS c")
                .bind(ardocoId)
                .to("id")
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }
}
