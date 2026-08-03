/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.architectureModel;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureModelNode;

@Repository
public interface ArchitectureModelRepository extends Neo4jRepository<ArchitectureModelNode, String> {

    @Query("""
                MATCH (m:ArchitectureModel {modelId: $modelId})
                // Traverse components, subcomponents, and interfaces
                OPTIONAL MATCH (m)-[:HAS_COMPONENT|HAS_INTERFACE|HAS_SUBCOMPONENT*0..10]->(item)
                // Traverse methods inside interfaces
                OPTIONAL MATCH (item)-[:HAS_METHOD]->(method)
                DETACH DELETE m, item, method
            """)
    void deleteByModelId(@Param("modelId") String modelId);

    @Override
    List<ArchitectureModelNode> findAll();
}
