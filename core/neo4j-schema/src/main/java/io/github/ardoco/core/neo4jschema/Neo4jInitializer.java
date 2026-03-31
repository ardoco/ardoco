package io.github.ardoco.core.neo4jschema;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@ConditionalOnProperty(name = "ardoco.persistence.neo4j.enabled", havingValue = "true")
public class Neo4jInitializer {
    private static final Logger logger = LoggerFactory.getLogger(Neo4jInitializer.class);
    private final Neo4jClient neo4jClient;

    public Neo4jInitializer(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @PostConstruct
    public void initializeNeo4j() {
        execute("CREATE INDEX sentence_num_idx IF NOT EXISTS FOR (s:Sentence) ON (s.sentenceNumber)");
        execute("CREATE INDEX traceable_id_idx IF NOT EXISTS FOR (t:Traceable) ON (t.ardocoId)");
        execute("CREATE INDEX inconsistency_reason_idx IF NOT EXISTS FOR (i:Inconsistency) ON (i.reason)");
        execute("CREATE INDEX inconsistency_id_idx IF NOT EXISTS FOR (i:Inconsistency) ON (i.id);");


        logger.info("Neo4j Indexes initialized successfully.");
    }

    private void execute(String cypher) {
        try {
            neo4jClient.query(cypher).run();
        } catch (Exception e) {
            logger.error("Failed to create index: {}", cypher, e);
        }
    }
}
