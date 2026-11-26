/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.ner;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.ner.NamedArchitectureEntityOccurrenceNode;

@Repository
public interface NamedArchitectureEntityOccurrenceRepository extends Neo4jRepository<NamedArchitectureEntityOccurrenceNode, String> {
}
