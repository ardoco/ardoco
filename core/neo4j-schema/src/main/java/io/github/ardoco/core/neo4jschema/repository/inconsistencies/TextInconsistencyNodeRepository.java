package io.github.ardoco.core.neo4jschema.repository.inconsistencies;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ModelInconsistencyNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TextInconsistencyNodeRepository extends Neo4jRepository<ModelInconsistencyNode, String> {

}
