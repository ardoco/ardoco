/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(name = "ardoco.persistence.neo4j.enabled", havingValue = "true")
public class Neo4jInitializer {
    private static final Logger logger = LoggerFactory.getLogger(Neo4jInitializer.class);
    private static final int ATTEMPTS = 5;
    private static final long RETRY_DELAY_MILLIS = 1200;
    private static final int AWAIT_INDEXES_SECONDS = 300;
    private final Neo4jClient neo4jClient;

    public Neo4jInitializer(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @PostConstruct
    public void initializeNeo4j() {
        execute("CREATE INDEX sentence_num_idx IF NOT EXISTS FOR (s:Sentence) ON (s.sentenceNumber)");
        execute("CREATE INDEX word_position_idx IF NOT EXISTS FOR (w:Word) ON (w.position)");
        execute("CREATE INDEX nounmapping_id_idx IF NOT EXISTS FOR (n:NounMapping) ON (n.ardocoId)");
        execute("CREATE INDEX recommendedinstance_id_idx IF NOT EXISTS FOR (r:RecommendedInstance) ON (r.ardocoId)");
        execute("CREATE INDEX phrase_id_idx IF NOT EXISTS FOR (p:Phrase) ON (p.id)");
        execute("CREATE INDEX traceable_id_idx IF NOT EXISTS FOR (t:Traceable) ON (t.ardocoId)");
        execute("CREATE INDEX inconsistency_reason_idx IF NOT EXISTS FOR (i:Inconsistency) ON (i.reason)");
        execute("CREATE INDEX inconsistency_id_idx IF NOT EXISTS FOR (i:Inconsistency) ON (i.id);");
        execute("CALL db.awaitIndexes(" + AWAIT_INDEXES_SECONDS + ")");

        logger.info("Neo4j Indexes initialized successfully.");
    }

    private void execute(String cypher) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            try {
                neo4jClient.query(cypher).run();
                return;
            } catch (RuntimeException e) {
                lastFailure = e;
                if (attempt < ATTEMPTS) {
                    sleepBeforeRetry();
                }
            }
        }
        logger.error("Failed to create index: {}", cypher, lastFailure);
        throw new IllegalStateException("Neo4j index setup failed: " + cypher, lastFailure);
    }

    private static void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
