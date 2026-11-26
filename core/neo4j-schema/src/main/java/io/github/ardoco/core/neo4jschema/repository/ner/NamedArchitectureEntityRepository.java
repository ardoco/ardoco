/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.ner;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.ner.NamedArchitectureEntityNode;

@Repository
public interface NamedArchitectureEntityRepository extends Neo4jRepository<NamedArchitectureEntityNode, String> {

    List<NamedArchitectureEntityNode> findByMetamodel(String metamodel);

    List<NamedArchitectureEntityNode> findByMetamodelAndUnlinked(String metamodel, boolean unlinked);

    boolean existsByMetamodel(String metamodel);
}
