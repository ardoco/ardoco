/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.documentation;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.documentation.SimpleTextNode;

@Repository
public interface SimpleTextRepository extends Neo4jRepository<SimpleTextNode, String> {

    Optional<SimpleTextNode> findByIdentifier(String identifier);

    boolean existsByIdentifier(String identifier);

    @Query("MATCH (s:SimpleText {identifier: $id}) DETACH DELETE s")
    void deleteByIdentifier(@Param("id") String id);
}
