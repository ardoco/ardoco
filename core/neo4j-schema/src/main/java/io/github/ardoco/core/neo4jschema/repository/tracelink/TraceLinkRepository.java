/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.tracelink;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;

@Repository
public interface TraceLinkRepository extends Neo4jRepository<ArchitectureItemNode, String> {

    /**
     * Fetches all ArchitectureNodes that have a trace link to code, including the relationship and the target code node.
     */
    @Query("MATCH (a:ArchitectureItem)-[r:TRACES_TO_CODE]->(c:CodeItem) RETURN a, collect(r), collect(c)")
    List<ArchitectureItemNode> findAllWithTraceLinks();
}
