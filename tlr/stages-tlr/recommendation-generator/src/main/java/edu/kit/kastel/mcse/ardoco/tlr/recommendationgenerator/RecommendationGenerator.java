/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.agents.InitialRecommendationAgent;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.agents.PhraseRecommendationAgent;

/**
 * The Class RecommendationGenerator defines the recommendation stage.
 */
public class RecommendationGenerator extends AbstractExecutionStage {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationGenerator.class);

    /**
     * Creates a new model connection agent with the given extraction state and ntr state.
     */
    public RecommendationGenerator(DataRepository dataRepository) {
        super(Lists.mutable.of(//
                //new TermBuilder(dataRepository),//
                new InitialRecommendationAgent(dataRepository),//
                new PhraseRecommendationAgent(dataRepository)),//
                RecommendationGenerator.class.getSimpleName(), dataRepository);
    }

    /**
     * Creates a {@link RecommendationGenerator} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of {@link RecommendationGenerator}
     */
    public static RecommendationGenerator get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var recommendationGenerator = new RecommendationGenerator(dataRepository);
        recommendationGenerator.applyConfiguration(additionalConfigs);
        return recommendationGenerator;
    }

    @Override
    protected void initializeState() {
        var dataRepository = this.getDataRepository();
        var activeMetamodels = dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow().getMetamodels();
        RecommendationStatesImpl recommendationStates = (RecommendationStatesImpl) RecommendationStatesImpl.build(activeMetamodels.toArray(Metamodel[]::new));
        hydrateFromPersistenceIfPresent(dataRepository, recommendationStates, activeMetamodels);
        dataRepository.addData(RecommendationStates.ID, recommendationStates);
    }

    /**
     * Load-on-resume: if Neo4j has RecommendedInstances, hydrate empty in-memory buckets once.
     * Requires TextState NounMappings already present (from prior stage or TextState resume).
     */
    private static void hydrateFromPersistenceIfPresent(DataRepository dataRepository, RecommendationStatesImpl recommendationStates,
            Collection<Metamodel> activeMetamodels) {
        if (!PersistenceBridge.shouldPersistRecommendations()) {
            return;
        }
        var handler = PersistenceBridge.getHandler();
        if (handler == null || !handler.hasRecommendedInstances()) {
            return;
        }
        if (dataRepository.getData(TextState.ID, TextState.class).isEmpty()) {
            logger.warn("Cannot load RecommendedInstances from Neo4j: TextState is not available");
            return;
        }
        TextState textState = dataRepository.getData(TextState.ID, TextState.class).orElseThrow();
        Map<String, NounMapping> nounMappingsById = new HashMap<>();
        for (NounMapping mapping : textState.getNounMappings()) {
            nounMappingsById.put(mapping.getArdocoId(), mapping);
        }

        int total = 0;
        for (Metamodel metamodel : activeMetamodels) {
            RecommendationStateImpl state = recommendationStates.getRecommendationState(metamodel);
            Collection<RecommendedInstance> loaded = handler.loadRecommendedInstances(metamodel, nounMappingsById);
            for (RecommendedInstance ri : loaded) {
                state.hydrateRecommendedInstance(ri);
            }
            total += loaded.size();
        }
        logger.info("Hydrated {} RecommendedInstances from Neo4j (resume)", total);
    }
}
