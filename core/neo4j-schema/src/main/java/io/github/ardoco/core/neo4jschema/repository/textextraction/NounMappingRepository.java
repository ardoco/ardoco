/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.textextraction;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.textextraction.NounMappingNode;

@Repository
public interface NounMappingRepository extends Neo4jRepository<NounMappingNode, String> {

    Optional<NounMappingNode> findByArdocoId(String ardocoId);

    @Query("MATCH (nm:NounMapping {ardocoId: $ardocoId}) DETACH DELETE nm")
    void deleteByArdocoId(@Param("ardocoId") String ardocoId);
}
