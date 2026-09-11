/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import io.github.ardoco.core.neo4jschema.entities.recommendation.RecommendedInstanceNode;
import io.github.ardoco.core.neo4jschema.mapper.RecommendedInstanceMapper;
import io.github.ardoco.core.neo4jschema.repository.recommendation.RecommendedInstanceRepository;

/**
 * Persists RecommendationStates with Spring Data Neo4j.
 * Dual-write today; {@link #loadRecommendedInstances} supports load-on-resume.
 */
@Service
public class RecommendationPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationPersistenceService.class);

    private final RecommendedInstanceRepository recommendedInstanceRepository;
    private final RecommendedInstanceMapper recommendedInstanceMapper;

    public RecommendationPersistenceService(RecommendedInstanceRepository recommendedInstanceRepository,
            RecommendedInstanceMapper recommendedInstanceMapper) {
        this.recommendedInstanceRepository = recommendedInstanceRepository;
        this.recommendedInstanceMapper = recommendedInstanceMapper;
    }

    @Transactional
    public void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel) {
        RecommendedInstanceNode node = recommendedInstanceMapper.toNode(recommendedInstance, metamodel);
        recommendedInstanceRepository.save(node);
        logger.debug("Saved RecommendedInstance {} ({})", recommendedInstance.getId(), recommendedInstance.getName());
    }

    public boolean hasRecommendedInstances() {
        return recommendedInstanceRepository.count() > 0;
    }

    /**
     * Loads recommended instances for a metamodel. {@code nounMappingsById} must already be hydrated
     * (TextState resume before RecommendationStates).
     */
    @Transactional(readOnly = true)
    public Collection<RecommendedInstance> loadRecommendedInstances(Metamodel metamodel, Map<String, NounMapping> nounMappingsById) {
        List<RecommendedInstance> result = new ArrayList<>();
        String metamodelName = metamodel.name();
        for (RecommendedInstanceNode node : recommendedInstanceRepository.findAll()) {
            if (!metamodelName.equals(node.getMetamodel())) {
                continue;
            }
            result.add(recommendedInstanceMapper.toDomain(node, nounMappingsById));
        }
        logger.info("Loaded {} RecommendedInstances for {} from Neo4j", result.size(), metamodelName);
        return result;
    }

    @Transactional(readOnly = true)
    public Collection<RecommendedInstance> loadAllRecommendedInstances(Map<String, NounMapping> nounMappingsById) {
        return StreamSupport.stream(recommendedInstanceRepository.findAll().spliterator(), false)
                .map(node -> recommendedInstanceMapper.toDomain(node, nounMappingsById))
                .collect(Collectors.toList());
    }
}
