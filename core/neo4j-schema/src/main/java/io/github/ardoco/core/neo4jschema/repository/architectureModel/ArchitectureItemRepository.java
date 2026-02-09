/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.architectureModel;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;

@Repository
public interface ArchitectureItemRepository extends Neo4jRepository<ArchitectureItemNode, String> {
    Optional<ArchitectureItemNode> findByArdocoId(String ardocoId);
}
