/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.recommendation;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.recommendation.RecommendedInstanceNode;

@Repository
public interface RecommendedInstanceRepository extends Neo4jRepository<RecommendedInstanceNode, String> {

    Optional<RecommendedInstanceNode> findByArdocoId(String ardocoId);

    @Query("MATCH (ri:RecommendedInstance {ardocoId: $ardocoId}) DETACH DELETE ri")
    void deleteByArdocoId(@Param("ardocoId") String ardocoId);
}
