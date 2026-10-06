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
 * Writes go through the {@code upsert…}/{@code link…} queries below instead of {@code save(RecommendedInstanceNode)}. A {@code save} needs fully
 * loaded NounMapping nodes and then cascades through their (cyclic) Word/Phrase subgraphs. The queries below only write the RecommendedInstance node
 * and its own outgoing edges; NounMapping nodes are matched by their indexed {@code ardocoId}, never loaded or written.
 */
@Repository
public interface RecommendedInstanceRepository extends Neo4jRepository<RecommendedInstanceNode, String> {

    Optional<RecommendedInstanceNode> findByArdocoId(String ardocoId);

    @Query("MATCH (ri:RecommendedInstance {ardocoId: $ardocoId}) DETACH DELETE ri")
    void deleteByArdocoId(@Param("ardocoId") String ardocoId);

    /**
     * Creates or updates the RecommendedInstance node and removes all of its outgoing HAS_NAME_MAPPING and HAS_TYPE_MAPPING edges, so that the
     * subsequent {@code link…} calls recreate them from the current domain state.
     *
     * @param ardocoId the ardocoId of the recommended instance
     * @param props    scalar properties; keys must equal the field names of {@link RecommendedInstanceNode}
     */
    @Query("""
            MERGE (ri:RecommendedInstance {ardocoId: $ardocoId})
            SET ri += $props
            WITH ri
            OPTIONAL MATCH (ri)-[old:HAS_NAME_MAPPING|HAS_TYPE_MAPPING]->()
            DELETE old
            """)
    void upsertAndClearMappings(@Param("ardocoId") String ardocoId, @Param("props") Map<String, Object> props);

    /**
     * Links the RecommendedInstance to the existing NounMapping nodes with the given ardocoIds as name mappings.
     *
     * @return the number of distinct NounMapping nodes linked
     */
    @Query("""
            MATCH (ri:RecommendedInstance {ardocoId: $ardocoId})
            UNWIND $nounMappingIds AS nmId
            MATCH (nm:NounMapping {ardocoId: nmId})
            MERGE (ri)-[:HAS_NAME_MAPPING]->(nm)
            RETURN count(DISTINCT nm)
            """)
    Long linkNameMappings(@Param("ardocoId") String ardocoId, @Param("nounMappingIds") List<String> nounMappingIds);

    /**
     * Links the RecommendedInstance to the existing NounMapping nodes with the given ardocoIds as type mappings.
     *
     * @return the number of distinct NounMapping nodes linked
     */
    @Query("""
            MATCH (ri:RecommendedInstance {ardocoId: $ardocoId})
            UNWIND $nounMappingIds AS nmId
            MATCH (nm:NounMapping {ardocoId: nmId})
            MERGE (ri)-[:HAS_TYPE_MAPPING]->(nm)
            RETURN count(DISTINCT nm)
            """)
    Long linkTypeMappings(@Param("ardocoId") String ardocoId, @Param("nounMappingIds") List<String> nounMappingIds);
}
