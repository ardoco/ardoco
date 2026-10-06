/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.documentation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.documentation.PhraseNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.WordNode;

/**
 * Lookup helpers for existing documentation graph nodes (created during text preprocessing).
 */
@Repository
public interface DocumentationGraphRepository extends Neo4jRepository<WordNode, String> {

    @Query("MATCH (w:Word) WHERE w.position IN $positions RETURN w")
    List<WordNode> findWordsByPositions(@Param("positions") Collection<Integer> positions);

    /**
     * Finds the Phrase whose direct {@code CONTAINS_WORD} positions match exactly.
     */
    @Query("""
            MATCH (p:Phrase {phraseType: $phraseType})-[:CONTAINS_WORD]->(w:Word)
            WITH p, collect(DISTINCT w.position) AS phrasePositions
            WHERE size(phrasePositions) = size($positions)
              AND ALL(pos IN $positions WHERE pos IN phrasePositions)
            RETURN p
            LIMIT 1
            """)
    Optional<PhraseNode> findPhraseByTypeAndExactWordPositions(@Param("phraseType") String phraseType, @Param("positions") Collection<Integer> positions);

    /**
     * Same matching rule as {@link #findPhraseByTypeAndExactWordPositions}, but returns only the Phrase id.
     * <p>
     * Use this on write paths: returning a {@link PhraseNode} from this {@link WordNode} repository yields a DTO projection without relationships,
     * and cascading a {@code save} into it produced orphan Phrase copies (TeaStore: +3846 Phrase nodes without tree edges).
     *
     * @return the id of the matching Phrase (at most one element)
     */
    @Query("""
            MATCH (p:Phrase {phraseType: $phraseType})-[:CONTAINS_WORD]->(w:Word)
            WITH p, collect(DISTINCT w.position) AS phrasePositions
            WHERE size(phrasePositions) = size($positions)
              AND ALL(pos IN $positions WHERE pos IN phrasePositions)
            RETURN p.id
            LIMIT 1
            """)
    List<String> findPhraseIdByTypeAndExactWordPositions(@Param("phraseType") String phraseType, @Param("positions") Collection<Integer> positions);
}
