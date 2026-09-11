/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.RecommendedInstanceImpl;
import io.github.ardoco.core.neo4jschema.entities.recommendation.RecommendedInstanceNode;
import io.github.ardoco.core.neo4jschema.entities.textextraction.NounMappingNode;
import io.github.ardoco.core.neo4jschema.repository.textextraction.NounMappingRepository;

/**
 * Maps between domain {@link RecommendedInstance} and Neo4j {@link RecommendedInstanceNode}.
 * Name/type mappings are linked to existing {@link NounMappingNode}s by {@code ardocoId}.
 */
@Component
public class RecommendedInstanceMapper {

    private static final Logger logger = LoggerFactory.getLogger(RecommendedInstanceMapper.class);

    private final NounMappingRepository nounMappingRepository;

    public RecommendedInstanceMapper(NounMappingRepository nounMappingRepository) {
        this.nounMappingRepository = nounMappingRepository;
    }

    public RecommendedInstanceNode toNode(RecommendedInstance recommendedInstance, Metamodel metamodel) {
        RecommendedInstanceNode node = new RecommendedInstanceNode(recommendedInstance.getId());
        node.setName(recommendedInstance.getName());
        node.setType(recommendedInstance.getType());
        node.setProbability(recommendedInstance.getProbability());
        node.setMetamodel(metamodel.name());
        node.setNameMappings(resolveNounMappingNodes(recommendedInstance.getNameMappings()));
        node.setTypeMappings(resolveNounMappingNodes(recommendedInstance.getTypeMappings()));
        return node;
    }

    /**
     * Restores a domain RecommendedInstance. {@code nounMappingsById} must contain the NounMappings
     * already loaded for this resume (TextState hydrate first).
     */
    public RecommendedInstance toDomain(RecommendedInstanceNode node, Map<String, NounMapping> nounMappingsById) {
        ImmutableList<NounMapping> nameMappings = resolveDomainMappings(node.getNameMappings(), nounMappingsById);
        ImmutableList<NounMapping> typeMappings = resolveDomainMappings(node.getTypeMappings(), nounMappingsById);

        return new RecommendedInstanceImpl(node.getName(), node.getType() != null ? node.getType() : "", node.getArdocoId(),
                NounMappingMapper.RESUME_CLAIMANT, node.getProbability(), nameMappings, typeMappings);
    }

    private List<NounMappingNode> resolveNounMappingNodes(ImmutableList<NounMapping> mappings) {
        List<NounMappingNode> nodes = new ArrayList<>();
        for (NounMapping mapping : mappings) {
            nounMappingRepository.findByArdocoId(mapping.getArdocoId()).ifPresentOrElse(nodes::add, () -> logger.warn(
                    "No NounMapping node {} while saving RecommendedInstance", mapping.getArdocoId()));
        }
        return nodes;
    }

    private static ImmutableList<NounMapping> resolveDomainMappings(List<NounMappingNode> nodes, Map<String, NounMapping> nounMappingsById) {
        List<NounMapping> result = new ArrayList<>();
        if (nodes == null) {
            return Lists.immutable.empty();
        }
        for (NounMappingNode node : nodes) {
            NounMapping mapping = nounMappingsById.get(node.getArdocoId());
            if (mapping != null) {
                result.add(mapping);
            } else {
                logger.warn("No domain NounMapping {} while loading RecommendedInstance", node.getArdocoId());
            }
        }
        return Lists.immutable.withAll(result);
    }
}
