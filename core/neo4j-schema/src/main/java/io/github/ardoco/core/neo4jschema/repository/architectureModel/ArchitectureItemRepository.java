/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.architectureModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;

@Repository
public interface ArchitectureItemRepository extends Neo4jRepository<ArchitectureItemNode, String> {
//    Optional<ArchitectureItemNode> findByArdocoId(String ardocoId);
    List<ArchitectureItemNode> findByArdocoId(String ardocoId);

    List<ArchitectureItemNode> findAllByArdocoIdIn(Collection<String> ids);

    @Query("UNWIND $links AS link " +
            "MATCH (a:ArchitectureItem {ardocoId: link.archId}) " +
            "MATCH (c:CodeItem {ardocoId: link.codeId}) " +
            "MERGE (a)-[r:TRACES_TO_CODE]->(c) " +
            "SET r.confidence = link.confidence, r.type = link.type")
    void batchSaveSamCodeTraceLinks(@Param("links") List<Map<String, Object>> links);
}
