/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.textextraction;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.textextraction.NounMappingNode;

/**
 * Repository for {@link NounMappingNode}s.
 * <p>
 * Writes go through {@link #saveWithLinks} instead of {@code save(NounMappingNode)}. A {@code save} cascades into the
 * attached Word/Phrase objects and synchronises their relationships with the (relationship-less) Java objects, which deletes NEXT_WORD/DEPENDENCY
 * edges and creates orphan Phrase copies. The queries below only write the NounMapping node and its own outgoing edges; Word and Phrase nodes are
 * matched, never written.
 */
@Repository
public interface NounMappingRepository extends Neo4jRepository<NounMappingNode, String> {

    Optional<NounMappingNode> findByArdocoId(String ardocoId);

    @Query("MATCH (nm:NounMapping {ardocoId: $ardocoId}) DETACH DELETE nm")
    void deleteByArdocoId(@Param("ardocoId") String ardocoId);

    /**
     * Creates or updates the NounMapping node and replaces its outgoing MAPS_WORD, HAS_REFERENCE_WORD and IN_PHRASE edges in one round trip.
     * Word nodes are matched by position and Phrase nodes by id (both indexed); neither is written.
     *
     * @param ardocoId           the ardocoId of the noun mapping
     * @param props              scalar properties; keys must equal the field names of {@link NounMappingNode}
     * @param wordPositions      distinct positions of the mapped words
     * @param referencePositions distinct positions of the reference words
     * @param phraseIds          distinct ids of the Phrase nodes the noun mapping occurs in
     * @return the number of requested link targets that were not found (0 if all edges were created)
     */
    @Query("""
            MERGE (nm:NounMapping {ardocoId: $ardocoId})
            SET nm += $props
            WITH nm
            OPTIONAL MATCH (nm)-[old:MAPS_WORD|HAS_REFERENCE_WORD|IN_PHRASE]->()
            DELETE old
            WITH DISTINCT nm
            OPTIONAL MATCH (w:Word) WHERE w.position IN $wordPositions
            WITH nm, collect(DISTINCT w) AS words
            FOREACH (word IN words | MERGE (nm)-[:MAPS_WORD]->(word))
            WITH nm, words
            OPTIONAL MATCH (r:Word) WHERE r.position IN $referencePositions
            WITH nm, words, collect(DISTINCT r) AS referenceWords
            FOREACH (referenceWord IN referenceWords | MERGE (nm)-[:HAS_REFERENCE_WORD]->(referenceWord))
            WITH nm, words, referenceWords
            OPTIONAL MATCH (p:Phrase) WHERE p.id IN $phraseIds
            WITH nm, words, referenceWords, collect(DISTINCT p) AS phrases
            FOREACH (phrase IN phrases | MERGE (nm)-[:IN_PHRASE]->(phrase))
            RETURN (size($wordPositions) - size(words)) + (size($referencePositions) - size(referenceWords)) + (size($phraseIds) - size(phrases))
            """)
    Long saveWithLinks(@Param("ardocoId") String ardocoId, @Param("props") Map<String, Object> props, @Param("wordPositions") List<Integer> wordPositions,
            @Param("referencePositions") List<Integer> referencePositions, @Param("phraseIds") List<String> phraseIds);

    /**
     * Batched form of {@link #saveWithLinks}: the same per-item upsert and edge replacement, for every item in one round trip (one {@code UNWIND}
     * instead of one call per noun mapping). Used by the stage-level write-behind (see {@code PersistenceBridge::withDeferredWrites}) to replace N
     * sequential transactions with one.
     * <p>
     * Each map in {@code items} has the same shape as the scalar parameters of {@link #saveWithLinks}: {@code ardocoId} (String), {@code props}
     * (Map), {@code wordPositions} (List of Integer), {@code referencePositions} (List of Integer), {@code phraseIds} (List of String).
     *
     * @return the total number of requested link targets that were not found, summed across all items
     */
    @Query("""
            UNWIND $items AS item
            MERGE (nm:NounMapping {ardocoId: item.ardocoId})
            SET nm += item.props
            WITH nm, item
            OPTIONAL MATCH (nm)-[old:MAPS_WORD|HAS_REFERENCE_WORD|IN_PHRASE]->()
            DELETE old
            WITH DISTINCT nm, item
            OPTIONAL MATCH (w:Word) WHERE w.position IN item.wordPositions
            WITH nm, item, collect(DISTINCT w) AS words
            FOREACH (word IN words | MERGE (nm)-[:MAPS_WORD]->(word))
            WITH nm, item, words
            OPTIONAL MATCH (r:Word) WHERE r.position IN item.referencePositions
            WITH nm, item, words, collect(DISTINCT r) AS referenceWords
            FOREACH (referenceWord IN referenceWords | MERGE (nm)-[:HAS_REFERENCE_WORD]->(referenceWord))
            WITH nm, item, words, referenceWords
            OPTIONAL MATCH (p:Phrase) WHERE p.id IN item.phraseIds
            WITH item, words, referenceWords, collect(DISTINCT p) AS phrases, nm
            FOREACH (phrase IN phrases | MERGE (nm)-[:IN_PHRASE]->(phrase))
            RETURN sum((size(item.wordPositions) - size(words)) + (size(item.referencePositions) - size(referenceWords)) + (size(item.phraseIds) - size(phrases))) AS totalMissing
            """)
    Long saveAllWithLinks(@Param("items") List<Map<String, Object>> items);
}
