/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.codeModel;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeModelNode;

@Repository
public interface CodeModelRepository extends Neo4jRepository<CodeModelNode, String> {

    /**
     * Deletes the code model with the given id, all of its items, and the inconsistencies attached to those items. Without the last part, a re-saved
     * model would leave orphaned inconsistency nodes behind.
     *
     * @param modelId the id of the code model
     */
    @Query("""
            MATCH (m:CodeModel {modelId: $modelId})
            OPTIONAL MATCH (m)-[:CONTAINS_CODE_ROOT|CONTAINS_CODE_ITEM|HAS_REPOSITORY_ITEM|EXTENDS|IMPLEMENTS|REFERENCES_DATATYPE*0..]->(n)
            OPTIONAL MATCH (n)-[:HAS_INCONSISTENCY]->(i:Inconsistency)
            DETACH DELETE m, n, i
            """)
    void deleteByModelId(@Param("modelId") String modelId);

    /**
     * Deletes all code models of the given metamodel, their items, and the inconsistencies attached to those items.
     *
     * @param metamodel the metamodel name
     */
    @Query("""
            MATCH (m:CodeModel {metamodel: $mt})
            OPTIONAL MATCH (m)-[:CONTAINS_CODE_ITEM|CONTAINS_CODE_ROOT|HAS_REPOSITORY_ITEM|EXTENDS|IMPLEMENTS|REFERENCES_DATATYPE*0..]->(child)
            OPTIONAL MATCH (child)-[:HAS_INCONSISTENCY]->(i:Inconsistency)
            DETACH DELETE m, child, i
            """)
    void deleteByMetamodel(@Param("mt") String metamodel);
}
