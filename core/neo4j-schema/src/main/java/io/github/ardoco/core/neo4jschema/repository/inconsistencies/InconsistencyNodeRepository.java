package io.github.ardoco.core.neo4jschema.repository.inconsistencies;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.InconsistencyNode;

@Repository
public interface InconsistencyNodeRepository extends Neo4jRepository<InconsistencyNode, String> {

    @Query("""
                MATCH (i:Inconsistency)
                OPTIONAL MATCH (t:Traceable)-[r:HAS_INCONSISTENCY]->(i)
                RETURN i, collect(r), collect(t)
            """)
    List<InconsistencyNode> findAllWithRelationships();

    @Query("MATCH (i:ModelInconsistency)<-[:HAS_INCONSISTENCY]-(t:Traceable) WHERE t.ardocoId IN $ids DETACH DELETE i")
    void deleteByModelUids(@Param("ids") List<String> ids);

    @Query("MATCH (i:TextInconsistency)<-[:HAS_INCONSISTENCY]-(s:Sentence) WHERE s.sentenceNumber IN $sentenceNumbers DETACH DELETE i")
    void deleteBySentenceNumbers(@Param("sentenceNumbers") List<Integer> sentenceNumbers);

    @Query("RETURN EXISTS { MATCH (t:Traceable {ardocoId: $ardocoId})-[:HAS_INCONSISTENCY]->(i:ModelInconsistency {reason: $reason}) }")
    boolean existsModelInconsistency(@Param("ardocoId") String ardocoId, @Param("reason") String reason);

    @Query("RETURN EXISTS { MATCH (s:Sentence {sentenceNumber: $num})-[:HAS_INCONSISTENCY]->(i:TextInconsistency {reason: $reason, type: $type}) }")
    boolean existsTextInconsistency(@Param("num") int num, @Param("reason") String reason, @Param("type") String type);

    @Query("MATCH (i:Inconsistency)<-[:HAS_INCONSISTENCY]-(:ArchitectureItem) DETACH DELETE i")
    void deleteInconsistenciesForArchitectureItems();

    @Query("MATCH (i:Inconsistency)<-[:HAS_INCONSISTENCY]-(:CodeItem) DETACH DELETE i")
    void deleteInconsistenciesForCodeItems();

}
