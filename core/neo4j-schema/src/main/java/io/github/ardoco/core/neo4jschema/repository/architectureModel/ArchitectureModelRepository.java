/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.architectureModel;

import java.util.List;
import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureModelNode;

@Repository
public interface ArchitectureModelRepository extends Neo4jRepository<ArchitectureModelNode, String> {
    /**
     * Loads the ArchitectureModel and its entire hierarchy (Components, Subcomponents, Interfaces, Methods).
     * <p>
     * explicit path matching excludes the [:TRACES_TO_CODE] relationship,
     * ensuring we don't accidentally load TraceLinks or CodeItems.
     */
    @Query("MATCH (m:ArchitectureModel {modelId: $modelId}) " + "OPTIONAL MATCH p=(m)-[:HAS_COMPONENT|HAS_INTERFACE|HAS_SUBCOMPONENT|PROVIDES_INTERFACE|REQUIRES_INTERFACE|HAS_METHOD*0..]->(n) " + "RETURN m, collect(nodes(p)), collect(relationships(p))")
    Optional<ArchitectureModelNode> findByModelId(@Param("modelId") String modelId);

    @Query("MATCH (m:ArchitectureModel {modelId: $modelId}) " +
            "OPTIONAL MATCH (m)-[:HAS_COMPONENT|HAS_INTERFACE]->(child) " +
            "OPTIONAL MATCH (child)-[:HAS_INTERFACE|HAS_METHOD]->(grandchild) " +
            "DETACH DELETE m, child, grandchild")
    void deleteByModelId(@Param("modelId") String modelId);

    @Override
    List<ArchitectureModelNode> findAll();
}
