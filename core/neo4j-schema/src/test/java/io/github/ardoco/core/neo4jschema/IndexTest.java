package io.github.ardoco.core.neo4jschema;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.core.Neo4jClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = io.github.ardoco.core.neo4jschema.Main.class, properties = "ardoco.persistence.neo4j.enabled=true")
@Import({Neo4jBridgeActivator.class, Neo4jInitializer.class})
public class IndexTest extends AbstractNeo4jTest {

    @Autowired
    private Neo4jClient neo4jClient;

    @Test
    void verifyIndexesExist() {
        var indexes = neo4jClient.query("SHOW INDEXES").fetch().all();
        boolean found = indexes.stream()
                .anyMatch(idx -> idx.get("name").equals("sentence_num_idx"));

        assertTrue(found, "Index should have been created on startup!");
    }
}
