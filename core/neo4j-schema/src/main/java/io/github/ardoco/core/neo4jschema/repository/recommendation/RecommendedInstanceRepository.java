/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.recommendation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.recommendation.RecommendedInstanceNode;

/**
 * Repository for {@link RecommendedInstanceNode}s.
 * <p>
 * Writes go through {@link #saveWithMappings} instead of {@code save(RecommendedInstanceNode)}. A {@code save} needs fully
 * loaded NounMapping nodes and then cascades through their (cyclic) Word/Phrase subgraphs. The queries below only write the RecommendedInstance node
 * and its own outgoing edges; NounMapping nodes are matched by their indexed {@code ardocoId}, never loaded or written.
 */
@Repository
public interface RecommendedInstanceRepository extends Neo4jRepository<RecommendedInstanceNode, String> {

    Optional<RecommendedInstanceNode> findByArdocoId(String ardocoId);

    @Query("MATCH (ri:RecommendedInstance {ardocoId: $ardocoId}) DETACH DELETE ri")
    void deleteByArdocoId(@Param("ardocoId") String ardocoId);

    /**
     * Creates or updates the RecommendedInstance node and replaces its outgoing HAS_NAME_MAPPING and HAS_TYPE_MAPPING edges in one round trip.
     * NounMapping nodes are matched by their indexed {@code ardocoId}; they are not written.
     *
     * @param ardocoId       the ardocoId of the recommended instance
     * @param props          scalar properties; keys must equal the field names of {@link RecommendedInstanceNode}
     * @param nameMappingIds distinct ardocoIds of the name mappings
     * @param typeMappingIds distinct ardocoIds of the type mappings
     * @return the number of requested NounMapping targets that were not found (0 if all edges were created)
     */
    @Query("""
            MERGE (ri:RecommendedInstance {ardocoId: $ardocoId})
            SET ri += $props
            WITH ri
            OPTIONAL MATCH (ri)-[old:HAS_NAME_MAPPING|HAS_TYPE_MAPPING]->()
            DELETE old
            WITH DISTINCT ri
            OPTIONAL MATCH (n:NounMapping) WHERE n.ardocoId IN $nameMappingIds
            WITH ri, collect(DISTINCT n) AS names
            FOREACH (nameMapping IN names | MERGE (ri)-[:HAS_NAME_MAPPING]->(nameMapping))
            WITH ri, names
            OPTIONAL MATCH (t:NounMapping) WHERE t.ardocoId IN $typeMappingIds
            WITH ri, names, collect(DISTINCT t) AS types
            FOREACH (typeMapping IN types | MERGE (ri)-[:HAS_TYPE_MAPPING]->(typeMapping))
            RETURN (size($nameMappingIds) - size(names)) + (size($typeMappingIds) - size(types))
            """)
    Long saveWithMappings(@Param("ardocoId") String ardocoId, @Param("props") Map<String, Object> props,
            @Param("nameMappingIds") List<String> nameMappingIds, @Param("typeMappingIds") List<String> typeMappingIds);
}
