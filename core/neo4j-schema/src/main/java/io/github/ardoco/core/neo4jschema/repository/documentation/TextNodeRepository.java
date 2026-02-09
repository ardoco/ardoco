/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.documentation;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;

@Repository
public interface TextNodeRepository extends Neo4jRepository<TextNode, String> {
    TextNode findByArdocoId(String ardocoId);

    boolean existsByArdocoId(String ardocoId);
}
