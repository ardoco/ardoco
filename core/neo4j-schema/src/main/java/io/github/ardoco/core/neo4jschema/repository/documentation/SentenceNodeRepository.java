package io.github.ardoco.core.neo4jschema.repository.documentation;

import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SentenceNodeRepository extends Neo4jRepository<SentenceNode, String> {
    Optional<SentenceNode> findBySentenceNumber(int sentenceNumber);
}
