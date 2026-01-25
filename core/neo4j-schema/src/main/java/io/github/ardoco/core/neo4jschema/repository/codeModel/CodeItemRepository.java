package io.github.ardoco.core.neo4jschema.repository.codeModel;

import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeItemNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeItemRepository extends Neo4jRepository<CodeItemNode, String> {
    Optional<CodeItemNode> findByArdocoId(String ardocoId);
}
