/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.codeModel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeItemNode;

@Repository
public interface CodeItemRepository extends Neo4jRepository<CodeItemNode, String> {
    Optional<CodeItemNode> findByArdocoId(String ardocoId);

    List<CodeItemNode> findAllByArdocoIdIn(Collection<String> ids);
}
