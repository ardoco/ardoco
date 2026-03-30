package io.github.ardoco.core.neo4jschema.repository;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

@Repository
public interface TraceableNodeRepository extends Neo4jRepository<TraceableNode, String> {

    Optional<TraceableNode> findByArdocoId(String ardocoId);

}
