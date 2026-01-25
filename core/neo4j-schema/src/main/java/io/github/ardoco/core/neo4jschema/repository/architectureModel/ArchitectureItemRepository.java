package io.github.ardoco.core.neo4jschema.repository.architectureModel;

import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArchitectureItemRepository extends Neo4jRepository<ArchitectureItemNode, String> {
    Optional<ArchitectureItemNode> findByArdocoId(String ardocoId);
}
