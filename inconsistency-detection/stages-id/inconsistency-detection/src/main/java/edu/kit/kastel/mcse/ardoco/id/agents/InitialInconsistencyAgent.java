/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.id.agents;

import java.util.List;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.id.informants.OccasionFilter;
import edu.kit.kastel.mcse.ardoco.id.informants.RecommendedInstanceProbabilityFilter;
import edu.kit.kastel.mcse.ardoco.id.informants.UnwantedWordsFilter;

public class InitialInconsistencyAgent extends PipelineAgent {

    public InitialInconsistencyAgent(DataRepository dataRepository) {
        super(List.of(new RecommendedInstanceProbabilityFilter(dataRepository), new OccasionFilter(dataRepository), new UnwantedWordsFilter(dataRepository)),
                InitialInconsistencyAgent.class.getSimpleName(), dataRepository);

    }

    @Override
    protected void initializeState() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var inconsistencyStates = Objects.requireNonNull(DataRepositoryHelper.getInconsistencyStates(dataRepository), "InconsistencyStates must be available");
        for (var model : modelStates.getMetamodels()) {
            var modelState = Objects.requireNonNull(modelStates.getModel(model), "Model must be available");
            Metamodel metamodel = modelState.getMetamodel();
            var inconsistencyState = inconsistencyStates.getInconsistencyState(metamodel);
            var recommendationState = Objects.requireNonNull(recommendationStates.getRecommendationState(metamodel),
                    "No recommendation state for metamodel " + metamodel);

            inconsistencyState.addRecommendedInstances(recommendationState.getRecommendedInstances().toImmutable());
        }
    }
}
