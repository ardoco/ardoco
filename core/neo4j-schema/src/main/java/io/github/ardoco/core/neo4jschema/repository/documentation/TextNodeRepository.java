/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.documentation;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.documentation.TextNode;

@Repository
public interface TextNodeRepository extends Neo4jRepository<TextNode, String> {

    Optional<TextNode> findByArdocoId(String ardocoId);

    boolean existsByArdocoId(String ardocoId);

    @Query("""
                MATCH (t:Text {ardocoId: $ardocoId})
                OPTIONAL MATCH (t)-[:HAS_SENTENCE]->(s)
                OPTIONAL MATCH (s)-[:CONTAINS_WORD|HAS_ROOT_PHRASE|DEPENDENCY|HAS_CHILD_PHRASE*0..3]->(child)
                DETACH DELETE t, s, child
            """)
    void deleteByArdocoId(@Param("ardocoId") String ardocoId);

    // Get only the Text metadata (ID, etc.) without any relationships
    @Query("MATCH (t:Text{ardocoId: $id}) RETURN t")
    Optional<TextNode> findTextById(@Param("id") String id);
}
