/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.codeModel;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeModelNode;

@Repository
public interface CodeModelRepository extends Neo4jRepository<CodeModelNode, String> {
    //    Optional<CodeModelNode> findByModelId(String modelId);
    Optional<CodeModelNode> findByModelId(@Param("modelId") String modelId);

    @Query("MATCH (m:CodeModel {modelId: $modelId}) " +
            "OPTIONAL MATCH (m)-[r:CONTAINS_CODE_ROOT|CONTAINS_CODE_ITEM|EXTENDS|IMPLEMENTS|REFERENCES_DATATYPE*0..]->(n)" +
            "DETACH DELETE m, n")
    void deleteByModelId(@Param("modelId") String modelId);

    @Query("MATCH (m:CodeModel {metamodel: $metamodel}) " +
            "OPTIONAL MATCH (m)-[r:CONTAINS_CODE_ROOT|CONTAINS_CODE_ITEM|EXTENDS|IMPLEMENTS|REFERENCES_DATATYPE*0..5]->(n) " +
            "RETURN m, collect(r), collect(n)")
    Optional<CodeModelNode> findFullModel(String metamodel);
}
