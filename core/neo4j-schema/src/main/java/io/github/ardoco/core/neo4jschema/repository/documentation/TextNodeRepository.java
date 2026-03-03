/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.documentation;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;

import java.util.Optional;

@Repository
public interface TextNodeRepository extends Neo4jRepository<TextNode, String> {
//    TextNode findByArdocoId(String ardocoId);

    Optional<TextNode> findByArdocoId(String ardocoId);

    boolean existsByArdocoId(String ardocoId);

    @Query("MATCH (t:Text)-[:HAS_SENTENCE]->(s:Sentence {ardocoId: $sentenceId}) RETURN t")
    TextNode findTextBySentenceId(@Param("sentenceNumber") int sentenceNumber);
}
