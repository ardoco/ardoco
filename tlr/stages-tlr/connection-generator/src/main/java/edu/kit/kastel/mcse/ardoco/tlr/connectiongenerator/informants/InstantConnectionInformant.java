/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.informants;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.LegacyModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class InstantConnectionInformant extends Informant {
    @Configurable
    private double probability = 1.0;
    @Configurable
    private double probabilityWithoutType = 0.8;

    public InstantConnectionInformant(DataRepository dataRepository) {
        super(InstantConnectionInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        DataRepository dataRepository = this.getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var connectionStates = DataRepositoryHelper.getConnectionStates(dataRepository);
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel metamodel = modelState.getMetamodel();
            var recommendationState = recommendationStates.getRecommendationState(metamodel);
            var connectionState = connectionStates.getConnectionState(metamodel);

            this.findNamesOfModelInstancesInSupposedMappings(modelState, recommendationState, connectionState);
            this.createLinksForEqualOrSimilarRecommendedInstances(modelState, recommendationState, connectionState);
        }
    }

    /**
     * Searches in the recommended instances of the recommendation state for similar names to extracted instances. If
     * some are found the instance link is added to the connection state.
     */
    private void findNamesOfModelInstancesInSupposedMappings(LegacyModelExtractionState modelState, RecommendationState recommendationState,
            ConnectionState connectionState) {
        var recommendedInstances = recommendationState.getRecommendedInstances();
        for (ModelInstance instance : modelState.getInstances()) {
            var mostLikelyRi = SimilarityUtils.getInstance().getMostRecommendedInstancesToInstanceByReferences(instance, recommendedInstances);

            for (var recommendedInstance : mostLikelyRi) {
                var riProbability = recommendedInstance.getTypeMappings().isEmpty() ? this.probabilityWithoutType : this.probability;
                connectionState.addToLinks(recommendedInstance, instance, this, riProbability);
            }
        }
    }

    private void createLinksForEqualOrSimilarRecommendedInstances(LegacyModelExtractionState modelState, RecommendationState recommendationState,
            ConnectionState connectionState) {
        for (var recommendedInstance : recommendationState.getRecommendedInstances()) {
            var sameInstances = modelState.getInstances()
                    .select(instance -> SimilarityUtils.getInstance().isRecommendedInstanceSimilarToModelInstance(recommendedInstance, instance));
            sameInstances.forEach(instance -> connectionState.addToLinks(recommendedInstance, instance, this, this.probability));
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> map) {
        // empty
    }
}
