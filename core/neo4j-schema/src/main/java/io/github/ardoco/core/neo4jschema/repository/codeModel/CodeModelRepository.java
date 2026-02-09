/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.codeModel;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeModelNode;

@Repository
public interface CodeModelRepository extends Neo4jRepository<CodeModelNode, String> {
    //    Optional<CodeModelNode> findByModelId(String modelId);
    Optional<CodeModelNode> findByModelId(@Param("modelId") String modelId);
}
