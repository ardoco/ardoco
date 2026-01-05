package io.github.ardoco.core.repository.codeModel;

import io.github.ardoco.core.entities.codeModel.CodeModelNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CodeModelRepository extends Neo4jRepository<CodeModelNode, String> {
    Optional<CodeModelNode> findByModelId(String modelId);
}
