/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.informants;

import java.util.SortedMap;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class ReferenceInformant extends Informant {

    @Configurable
    private final double probability = 0.75;

    public ReferenceInformant(DataRepository dataRepository) {
        super(ReferenceInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        DataRepository dataRepository = this.getDataRepository();
        var textState = DataRepositoryHelper.getTextState(dataRepository);
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelExtractionState(model);
            var recommendationState = recommendationStates.getRecommendationState(modelState.getMetamodel());
            this.findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances(modelState, recommendationState, textState);
        }
    }

    /**
     * Searches for instances mentioned in the text extraction state as names. If it founds some similar names it creates recommendations.
     */
    private void findRecommendedInstancesFromNounMappingsThatAreSimilarToInstances(Model modelState, RecommendationState recommendationState,
            TextState textState) {
        for (ModelInstance instance : modelState.getInstances()) {
            var similarToInstanceMappings = this.getSimilarNounMappings(instance, textState);

            for (NounMapping similarNameMapping : similarToInstanceMappings) {
                recommendationState.addRecommendedInstance(similarNameMapping.getReference(), this, this.probability, similarToInstanceMappings);
            }
        }

    }

    private ImmutableList<NounMapping> getSimilarNounMappings(ModelInstance instance, TextState textState) {
        return textState.getNounMappingsOfKind(MappingKind.NAME)
                .select(nounMapping -> SimilarityUtils.getInstance().isNounMappingSimilarToModelInstance(nounMapping, instance));
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> map) {
        // empty
    }
}
