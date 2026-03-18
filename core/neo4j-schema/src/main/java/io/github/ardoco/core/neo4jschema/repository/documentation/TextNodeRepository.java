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

    /**
     * Consolidated Fast Delete.
     * DETACH DELETE ensures we clean up TraceLinks and Inconsistencies pointing to these nodes.
     */
    @Query("""
        MATCH (t:Text {ardocoId: $ardocoId})
        OPTIONAL MATCH (t)-[:HAS_SENTENCE]->(s:Sentence)
        OPTIONAL MATCH (s)-[r:TRACES_TO]->()
        OPTIONAL MATCH (s)-[:HAS_INCONSISTENCY]->(i:Inconsistency)
        OPTIONAL MATCH (s)-[:CONTAINS_WORD|HAS_ROOT_PHRASE|HAS_CHILD_PHRASE*0..15]->(content)
        DETACH DELETE t, s, r, i, content
    """)
    void deleteByArdocoIdFast(@Param("ardocoId") String ardocoId);

    /**
     * Optimized Deep Fetch.
     * Using a single path match where possible improves Cypher performance.
     */
    @Query("""
        MATCH (t:Text {ardocoId: $ardocoId})
        OPTIONAL MATCH (t)-[r1:HAS_SENTENCE]->(s:Sentence)
        OPTIONAL MATCH (s)-[r2:CONTAINS_WORD]->(w:Word)
        OPTIONAL MATCH (w)-[r3:DEPENDENCY]->(target:Word)
        OPTIONAL MATCH (s)-[r4:HAS_ROOT_PHRASE]->(rp:Phrase)
        OPTIONAL MATCH (rp)-[r5:HAS_CHILD_PHRASE|CONTAINS_WORD*1..6]->(sub)
        RETURN t, collect(r1), collect(s), collect(r2), collect(w), 
               collect(r3), collect(target), collect(r4), collect(rp), collect(r5), collect(sub)
    """)
    Optional<TextNode> findByArdocoIdDeep(@Param("ardocoId") String ardocoId);

}
