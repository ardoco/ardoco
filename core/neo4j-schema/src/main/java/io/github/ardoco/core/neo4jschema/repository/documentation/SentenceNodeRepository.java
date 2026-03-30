package io.github.ardoco.core.neo4jschema.repository.documentation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;

@Repository
public interface SentenceNodeRepository extends Neo4jRepository<SentenceNode, String> {
    Optional<SentenceNode> findBySentenceNumber(int sentenceNumber);

    @Query("""
            MATCH (t:Text {ardocoId: $id})-[:HAS_SENTENCE]->(s:Sentence)
            WHERE s.sentenceNumber >= $skip AND s.sentenceNumber < ($skip + $limit)
            
            OPTIONAL MATCH (s)-[:CONTAINS_WORD]->(w:Word)
            WITH s, collect(DISTINCT w) AS words
            
            OPTIONAL MATCH (s)-[:HAS_ROOT_PHRASE]->(root:Phrase)
            OPTIONAL MATCH (root)-[:HAS_CHILD_PHRASE*0..]->(p:Phrase)
            
            WITH s, words, 
                 collect(DISTINCT elementId(root)) AS rootIds, 
                 collect(DISTINCT p) AS allPhrases
            
            RETURN s {.*, 
                      words: [word IN words | word {.*}], 
                      rootPhraseIds: rootIds,
                      phrases: [phrase IN allPhrases | phrase { 
                          .*, 
                          id: elementId(phrase),
                          childIds: [(phrase)-[:HAS_CHILD_PHRASE]->(child) | elementId(child)],
                          containedWords: [(phrase)-[:CONTAINS_WORD]->(cw:Word) | cw.position]
                      }]
                     } AS sentenceData
            ORDER BY s.sentenceNumber ASC
            """)
    List<Map<String, Object>> findPagedSentenceData(@Param("id") String id, @Param("skip") int skip, @Param("limit") int limit);
}
