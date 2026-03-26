package io.github.ardoco.core.neo4jschema.repository.inconsistencies;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.TextInconsistencyNode;

@Repository
public interface TextInconsistencyNodeRepository extends Neo4jRepository<TextInconsistencyNode, String> {

    @Query("MATCH (n:TextInconsistency) DETACH DELETE n")
    void deleteAllTextInconsistencies();
}
