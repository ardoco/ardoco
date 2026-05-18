/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.tracelink;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkType;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

@Repository
public interface TraceLinkRepository extends Neo4jRepository<TraceableNode, String> {

    /**
     * Finds all TraceableNodes that have a relationship of a specific TraceLinkType. This query traverses the 'TRACES_TO' relationship and filters by the
     * property.
     */
    @Query("""
            MATCH (source:Traceable)-[r:TRACES_TO]->(target:Traceable)
            WHERE r.traceLinkType = $type
            RETURN source, collect(r), collect(target)
            """)
    List<TraceableNode> findAllByRelationshipType(@Param("type") TraceLinkType type);

    @Query("""
            MATCH (s:Traceable {ardocoId: $sourceId}), (t:Traceable {ardocoId: $targetId})
            MERGE (s)-[r:TRACES_TO {traceLinkType: $type}]->(t)
            SET r.confidence = COALESCE($conf, -1.0),
                r.traceLinkType = $type
            """)
    void createTraceLink(@Param("sourceId") String sourceId, @Param("targetId") String targetId, @Param("conf") Double conf, @Param("type") TraceLinkType type);

    default void createTraceLink(String sourceId, String targetId, TraceLinkType type) {
        createTraceLink(sourceId, targetId, -1.0, type);
    }

    @Query("MATCH ()-[r:TRACES_TO]->()  DETACH DELETE r")
    void deleteAllTraceLinks();

    @Query("MATCH ()-[r:TRACES_TO]->() WHERE r.traceLinkType = $type DELETE r")
    void deleteLinksByType(@Param("type") TraceLinkType type);
}
