package io.github.ardoco.core.neo4jschema.repository.documentation;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;

@Repository
public interface SentenceNodeRepository extends Neo4jRepository<SentenceNode, String> {
    Optional<SentenceNode> findBySentenceNumber(int sentenceNumber);

}
