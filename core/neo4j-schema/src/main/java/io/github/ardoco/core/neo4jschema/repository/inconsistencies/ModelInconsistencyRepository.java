package io.github.ardoco.core.neo4jschema.repository.inconsistencies;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ModelInconsistencyNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelInconsistencyRepository extends Neo4jRepository<ModelInconsistencyNode, String> {

}
