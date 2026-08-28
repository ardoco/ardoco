/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;

/**
 * Write-only persistence of RecommendationStates.
 * Links to existing {@code NounMapping} nodes created when TextState dual-write is enabled.
 */
@Service
public class RecommendationPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationPersistenceService.class);

    private static final String UPSERT_NODE = """
            MERGE (ri:RecommendedInstance {ardocoId: $ardocoId})
            SET ri.name = $name,
                ri.type = $type,
                ri.probability = $probability,
                ri.metamodel = $metamodel
            WITH ri
            OPTIONAL MATCH (ri)-[old:HAS_NAME_MAPPING|HAS_TYPE_MAPPING]->()
            DELETE old
            """;

    private static final String LINK_NAME_MAPPINGS = """
            MATCH (ri:RecommendedInstance {ardocoId: $ardocoId})
            UNWIND $mappingIds AS nid
            MATCH (nm:NounMapping {ardocoId: nid})
            MERGE (ri)-[:HAS_NAME_MAPPING]->(nm)
            """;

    private static final String LINK_TYPE_MAPPINGS = """
            MATCH (ri:RecommendedInstance {ardocoId: $ardocoId})
            UNWIND $mappingIds AS nid
            MATCH (nm:NounMapping {ardocoId: nid})
            MERGE (ri)-[:HAS_TYPE_MAPPING]->(nm)
            """;

    private final Neo4jClient neo4jClient;

    public RecommendationPersistenceService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Transactional
    public void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel) {
        String ardocoId = recommendedInstance.getId();
        List<String> nameIds = recommendedInstance.getNameMappings().collect(NounMapping::getArdocoId).toList();
        List<String> typeIds = recommendedInstance.getTypeMappings().collect(NounMapping::getArdocoId).toList();

        neo4jClient.query(UPSERT_NODE)
                .bind(ardocoId)
                .to("ardocoId")
                .bind(recommendedInstance.getName())
                .to("name")
                .bind(recommendedInstance.getType())
                .to("type")
                .bind(recommendedInstance.getProbability())
                .to("probability")
                .bind(metamodel.name())
                .to("metamodel")
                .run();

        if (!nameIds.isEmpty()) {
            neo4jClient.query(LINK_NAME_MAPPINGS).bind(ardocoId).to("ardocoId").bind(nameIds).to("mappingIds").run();
        }
        if (!typeIds.isEmpty()) {
            neo4jClient.query(LINK_TYPE_MAPPINGS).bind(ardocoId).to("ardocoId").bind(typeIds).to("mappingIds").run();
        }
        logger.debug("Saved RecommendedInstance {} ({} name mappings, {} type mappings)", ardocoId, nameIds.size(), typeIds.size());
    }
}
