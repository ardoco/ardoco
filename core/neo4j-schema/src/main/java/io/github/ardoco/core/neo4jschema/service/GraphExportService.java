/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import edu.kit.kastel.mcse.ardoco.core.common.JsonHandling;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Exports the entire Neo4j graph as a lossless JSON dump (every node and relationship, with labels/type and
 * properties). Used to keep a Neo4j-mode run's knowledge graph on disk after the live database is wiped at the end
 * of the run; interim format until the final knowledge-graph format is researched (see the project notes).
 *
 * <p>No APOC dependency: built from plain Cypher via {@link Neo4jClient}, so it works against Neo4j Desktop, Docker
 * and Testcontainers alike. Node/relationship identity in the dump uses {@code elementId()} (Neo4j 5.x), which is
 * stable only within one export, never across a wipe - exactly the one-run lifetime this export is for.
 *
 * <p>Trace links appear in the dump as {@code TRACES_TO} relationships with the properties {@code traceLinkType} and {@code confidence}. ArDoCo's final
 * SAD-to-code result has no relationship type of its own; how to read it from the dump (in the {@code Transarc} pipeline a two-hop join of
 * {@code SENTENCE_ARCHITECTURE} and {@code ARCHITECTURE_CODE} relationships) is described in {@link TraceLinkPersistenceService}.
 */
@Service
public class GraphExportService {
    private static final String NODES_QUERY = """
            MATCH (n)
            RETURN elementId(n) AS id, labels(n) AS labels, properties(n) AS properties
            """;

    private static final String RELATIONSHIPS_QUERY = """
            MATCH ()-[r]->()
            RETURN elementId(r) AS id, type(r) AS type, elementId(startNode(r)) AS startNodeId,
                   elementId(endNode(r)) AS endNodeId, properties(r) AS properties
            """;

    private final Neo4jClient neo4jClient;
    private final ObjectMapper objectMapper;

    public GraphExportService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
        this.objectMapper = JsonHandling.createObjectMapper();
    }

    /**
     * @return a lossless, pretty-printed JSON dump of every node and relationship currently in the graph
     */
    public String exportAsJson() {
        List<Map<String, Object>> nodes = new ArrayList<>(this.neo4jClient.query(NODES_QUERY).fetch().all());
        List<Map<String, Object>> relationships = new ArrayList<>(this.neo4jClient.query(RELATIONSHIPS_QUERY).fetch().all());

        Map<String, Object> dump = new LinkedHashMap<>();
        dump.put("nodeCount", nodes.size());
        dump.put("relationshipCount", relationships.size());
        dump.put("nodes", nodes);
        dump.put("relationships", relationships);

        try {
            return this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dump);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize the Neo4j graph export to JSON", e);
        }
    }
}
