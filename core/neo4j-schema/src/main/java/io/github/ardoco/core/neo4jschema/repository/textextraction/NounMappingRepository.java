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
 * Writes go through the {@code upsert…}/{@code link…} queries below instead of {@code save(NounMappingNode)}. A {@code save} cascades into the
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
     * Creates or updates the NounMapping node and removes all of its outgoing MAPS_WORD, HAS_REFERENCE_WORD and IN_PHRASE edges, so that the
     * subsequent {@code link…} calls recreate them from the current domain state.
     *
     * @param ardocoId the ardocoId of the noun mapping
     * @param props    scalar properties; keys must equal the field names of {@link NounMappingNode}
     */
    @Query("""
            MERGE (nm:NounMapping {ardocoId: $ardocoId})
            SET nm += $props
            WITH nm
            OPTIONAL MATCH (nm)-[old:MAPS_WORD|HAS_REFERENCE_WORD|IN_PHRASE]->()
            DELETE old
            """)
    void upsertAndClearLinks(@Param("ardocoId") String ardocoId, @Param("props") Map<String, Object> props);

    /**
     * Links the NounMapping to the existing Word nodes at the given positions.
     *
     * @return the number of distinct Word nodes linked
     */
    @Query("""
            MATCH (nm:NounMapping {ardocoId: $ardocoId})
            UNWIND $positions AS pos
            MATCH (w:Word {position: pos})
            MERGE (nm)-[:MAPS_WORD]->(w)
            RETURN count(DISTINCT w)
            """)
    Long linkMappedWords(@Param("ardocoId") String ardocoId, @Param("positions") List<Integer> positions);

    /**
     * Links the NounMapping to its reference Word nodes at the given positions.
     *
     * @return the number of distinct Word nodes linked
     */
    @Query("""
            MATCH (nm:NounMapping {ardocoId: $ardocoId})
            UNWIND $positions AS pos
            MATCH (w:Word {position: pos})
            MERGE (nm)-[:HAS_REFERENCE_WORD]->(w)
            RETURN count(DISTINCT w)
            """)
    Long linkReferenceWords(@Param("ardocoId") String ardocoId, @Param("positions") List<Integer> positions);

    /**
     * Links the NounMapping to the existing Phrase nodes (from the preprocessed text graph) with the given ids.
     *
     * @return the number of distinct Phrase nodes linked
     */
    @Query("""
            MATCH (nm:NounMapping {ardocoId: $ardocoId})
            UNWIND $phraseIds AS pid
            MATCH (p:Phrase {id: pid})
            MERGE (nm)-[:IN_PHRASE]->(p)
            RETURN count(DISTINCT p)
            """)
    Long linkPhrases(@Param("ardocoId") String ardocoId, @Param("phraseIds") List<String> phraseIds);
}
