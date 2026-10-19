/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.runmetadata;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.runmetadata.RunMetadataNode;

@Repository
public interface RunMetadataRepository extends Neo4jRepository<RunMetadataNode, String> {
}
