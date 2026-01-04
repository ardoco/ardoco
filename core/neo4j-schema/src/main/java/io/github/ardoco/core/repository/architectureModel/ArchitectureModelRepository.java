package io.github.ardoco.core.repository.architectureModel;

import io.github.ardoco.core.entities.architectureModel.ArchitectureModelNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArchitectureModelRepository extends Neo4jRepository<ArchitectureModelNode, String> {
    Optional<ArchitectureModelNode> findByModelId(String modelId);
}
