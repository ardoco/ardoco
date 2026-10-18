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

    /**
     * Deletes the architecture model with the given id, its components, interfaces and methods, and the inconsistencies attached to them. Without the
     * last part, a re-saved model would leave orphaned inconsistency nodes behind.
     *
     * @param modelId the id of the architecture model
     */
    @Query("""
                MATCH (m:ArchitectureModel {modelId: $modelId})
                // Traverse components, subcomponents, and interfaces - including interfaces reachable only via a
                // component's PROVIDES_INTERFACE/REQUIRES_INTERFACE (e.g. PCM models, which never attach interfaces
                // to the model directly via HAS_INTERFACE); missing these left them - and their methods - behind on
                // delete, masked only by Spring Data's id-aware upsert on a later save().
                OPTIONAL MATCH (m)-[:HAS_COMPONENT|HAS_INTERFACE|HAS_SUBCOMPONENT|PROVIDES_INTERFACE|REQUIRES_INTERFACE*0..10]->(item)
                // Traverse methods inside interfaces
                OPTIONAL MATCH (item)-[:HAS_METHOD]->(method)
                // Inconsistencies attached to items or methods
                OPTIONAL MATCH (item)-[:HAS_INCONSISTENCY]->(itemInconsistency:Inconsistency)
                OPTIONAL MATCH (method)-[:HAS_INCONSISTENCY]->(methodInconsistency:Inconsistency)
                DETACH DELETE m, item, method, itemInconsistency, methodInconsistency
            """)
    void deleteByModelId(@Param("modelId") String modelId);

    @Override
    List<ArchitectureModelNode> findAll();
}
