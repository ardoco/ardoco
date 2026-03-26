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

    @Query("""
        MATCH (t:Text {ardocoId: $ardocoId})
        OPTIONAL MATCH (t)-[:HAS_SENTENCE]->(s)
        OPTIONAL MATCH (s)-[:CONTAINS_WORD|HAS_ROOT_PHRASE|DEPENDENCY|HAS_CHILD_PHRASE*0..3]->(child)
        DETACH DELETE t, s, child
    """)
    void deleteByArdocoId(@Param("ardocoId") String ardocoId);

    @Query("""
    MATCH (t:Text {ardocoId: $ardocoId})
    OPTIONAL MATCH (t)-[r1:HAS_SENTENCE]->(s:Sentence)
    OPTIONAL MATCH (s)-[r2:CONTAINS_WORD]->(w:Word)
    // Fetch Dependencies
    OPTIONAL MATCH (w)-[r5:DEPENDENCY]->(target:Word)
    OPTIONAL MATCH (s)-[r3:HAS_ROOT_PHRASE]->(rp:Phrase)
    OPTIONAL MATCH (rp)-[r4:HAS_CHILD_PHRASE|CONTAINS_WORD*1..5]->(sub)
    RETURN t, 
           collect(r1), collect(s), 
           collect(r2), collect(w), 
           collect(r5), collect(target),
           collect(r3), collect(rp),
           collect(r4), collect(sub)
    """)
    Optional<TextNode> findByArdocoIdDeep(@Param("ardocoId") String ardocoId);

    @Query("MATCH (t:Text)-[:HAS_SENTENCE]->(s:Sentence {ardocoId: $sentenceId}) RETURN t")
    TextNode findTextBySentenceId(@Param("sentenceNumber") int sentenceNumber);
}
