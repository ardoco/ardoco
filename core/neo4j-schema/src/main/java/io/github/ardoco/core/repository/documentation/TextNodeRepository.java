package io.github.ardoco.core.repository.documentation;

import io.github.ardoco.core.entities.documentation.TextNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TextNodeRepository extends Neo4jRepository<TextNode, String> {
    TextNode findByArdocoId(String ardocoId);
}
