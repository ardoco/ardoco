package io.github.ardoco.core.neo4jschema.repository;

import io.github.ardoco.core.neo4jschema.entities.inconsistencies.ModelInconsistencyNode;

import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TraceableNodeRepository  extends Neo4jRepository<TraceableNode, String> {

     Optional<TraceableNode> findByArdocoId(String ardocoId);

}
