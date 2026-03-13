package io.github.ardoco.core.neo4jschema.repository.inconsistencies;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.InconsistencyNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InconsistencyNodeRepository extends Neo4jRepository<InconsistencyNode, String> {

    @Query("""
        MATCH (i:Inconsistency)
        OPTIONAL MATCH (t:Traceable)-[r:HAS_INCONSISTENCY]->(i)
        RETURN i, collect(r), collect(t)
    """)
    List<InconsistencyNode> findAllWithRelationships();
}
