package io.github.ardoco.core.neo4jschema.repository.codeModel;

import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeModelNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CodeModelRepository extends Neo4jRepository<CodeModelNode, String> {
//    Optional<CodeModelNode> findByModelId(String modelId);
    Optional<CodeModelNode> findByModelId(@Param("modelId") String modelId);
}
