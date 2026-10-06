/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

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

/**
 * Maps between domain {@link RecommendedInstance} and Neo4j {@link RecommendedInstanceNode}.
 * Name/type mappings are linked to existing {@link NounMappingNode}s by {@code ardocoId} (see {@code RecommendedInstanceRepository}).
 */
@Component
public class RecommendedInstanceMapper {

    private static final Logger logger = LoggerFactory.getLogger(RecommendedInstanceMapper.class);

    /**
     * Scalar properties of the RecommendedInstance node, for {@code RecommendedInstanceRepository#upsertAndClearMappings}.
     * <p>
     * The keys must equal the field names of {@link RecommendedInstanceNode}: Spring Data maps those fields 1:1, and the read path
     * ({@code findAll} + {@link #toDomain}) relies on the same names.
     *
     * @param recommendedInstance the domain recommended instance
     * @param metamodel           the metamodel of the owning recommendation state
     * @return property map (mutable, may contain {@code null} values)
     */
    public Map<String, Object> toProperties(RecommendedInstance recommendedInstance, Metamodel metamodel) {
        Map<String, Object> props = new HashMap<>();
        props.put("name", recommendedInstance.getName());
        props.put("type", recommendedInstance.getType());
        props.put("probability", recommendedInstance.getProbability());
        props.put("metamodel", metamodel.name());
        return props;
    }

    /**
     * Distinct ardocoIds of the given noun mappings, in iteration order.
     *
     * @param mappings the noun mappings
     * @return their distinct ardocoIds
     */
    public static List<String> ardocoIdsOf(ImmutableList<NounMapping> mappings) {
        Set<String> ids = new LinkedHashSet<>();
        for (NounMapping mapping : mappings) {
            ids.add(mapping.getArdocoId());
        }
        return new ArrayList<>(ids);
    }

    /**
     * Restores a domain RecommendedInstance. {@code nounMappingsById} must contain the NounMappings
     * already loaded for this resume (TextState hydrate first).
     */
    public RecommendedInstance toDomain(RecommendedInstanceNode node, SortedMap<String, NounMapping> nounMappingsById) {
        ImmutableList<NounMapping> nameMappings = resolveDomainMappings(node.getNameMappings(), nounMappingsById);
        ImmutableList<NounMapping> typeMappings = resolveDomainMappings(node.getTypeMappings(), nounMappingsById);

        return new RecommendedInstanceImpl(node.getName(), node.getType() != null ? node.getType() : "", node.getArdocoId(),
                NounMappingMapper.RESUME_CLAIMANT, node.getProbability(), nameMappings, typeMappings);
    }

    private static ImmutableList<NounMapping> resolveDomainMappings(List<NounMappingNode> nodes, SortedMap<String, NounMapping> nounMappingsById) {
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
