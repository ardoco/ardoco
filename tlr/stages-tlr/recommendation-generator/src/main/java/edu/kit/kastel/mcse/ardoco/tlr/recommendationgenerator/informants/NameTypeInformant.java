/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.informants;

import java.util.Objects;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.LegacyModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextStateStrategy;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.TextStateStrategies;

/**
 * This analyzer searches for name type patterns. If these patterns occur recommendations are created.
 *
 */

public class NameTypeInformant extends Informant {

    @Configurable
    private double probability = 1.0;
    private TextStateStrategies tss;

    /**
     * Creates a new NameTypeAnalyzer
     */
    public NameTypeInformant(DataRepository dataRepository, TextStateStrategies tss) {
        super(NameTypeInformant.class.getSimpleName(), dataRepository);
        this.tss = tss;
    }

    @Override
    public void process() {
        DataRepository dataRepository = this.getDataRepository();
        var text = DataRepositoryHelper.getAnnotatedText(dataRepository);
        var textState = DataRepositoryHelper.getTextState(dataRepository);
        var textStateStrategy = this.tss.apply(this.getDataRepository());
        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);

        for (var word : text.words()) {
            this.exec(textState, textStateStrategy, modelStatesData, recommendationStates, word);
        }
    }

    private void exec(TextState textState, TextStateStrategy tss, ModelStates modelStates, RecommendationStates recommendationStates, Word word) {
        for (var model : modelStates.modelIds()) {
            var modelState = modelStates.getModelExtractionState(model);
            var recommendationState = recommendationStates.getRecommendationState(modelState.getMetamodel());

            this.addRecommendedInstanceIfNameAfterType(textState, tss, word, modelState, recommendationState);
            this.addRecommendedInstanceIfNameBeforeType(textState, tss, word, modelState, recommendationState);
            this.addRecommendedInstanceIfNameOrTypeBeforeType(textState, tss, word, modelState, recommendationState);
            this.addRecommendedInstanceIfNameOrTypeAfterType(textState, tss, word, modelState, recommendationState);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameBeforeType(TextState textExtractionState, TextStateStrategy tss, Word word, LegacyModelExtractionState modelState,
            RecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var similarTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!similarTypes.isEmpty()) {
            tss.addNounMapping(word, MappingKind.TYPE, this, this.probability);

            var nameMappings = textExtractionState.getMappingsThatCouldBeOfKind(word.getPreWord(), MappingKind.NAME);
            var typeMappings = textExtractionState.getMappingsThatCouldBeOfKind(word, MappingKind.TYPE);

            CommonUtilities.addRecommendedInstancesFromNounMappings(similarTypes, nameMappings, typeMappings, recommendationState, this, this.probability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the names of the text extraction state
     * contain the following node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameAfterType(TextState textExtractionState, TextStateStrategy tss, Word word, LegacyModelExtractionState modelState,
            RecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            tss.addNounMapping(word, MappingKind.TYPE, this, this.probability);

            var typeMappings = textExtractionState.getMappingsThatCouldBeOfKind(word, MappingKind.TYPE);
            var nameMappings = textExtractionState.getMappingsThatCouldBeOfKind(word.getNextWord(), MappingKind.NAME);

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nameMappings, typeMappings, recommendationState, this, this.probability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the previous node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameOrTypeBeforeType(TextState textExtractionState, TextStateStrategy tss, Word word,
            LegacyModelExtractionState modelState, RecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);

        if (!sameLemmaTypes.isEmpty()) {
            tss.addNounMapping(word, MappingKind.TYPE, this, this.probability);

            var typeMappings = textExtractionState.getMappingsThatCouldBeOfKind(word, MappingKind.TYPE);
            var nortMappings = textExtractionState.getMappingsThatCouldBeMultipleKinds(word.getPreWord(), MappingKind.NAME, MappingKind.TYPE);

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nortMappings, typeMappings, recommendationState, this, this.probability);
        }
    }

    /**
     * Checks if the current node is a type in the text extraction state. If the name_or_types of the text extraction
     * state contain the afterwards node. If that's the case a recommendation for the combination of both is created.
     *
     * @param textExtractionState text extraction state
     * @param word                the current word
     */
    private void addRecommendedInstanceIfNameOrTypeAfterType(TextState textExtractionState, TextStateStrategy tss, Word word,
            LegacyModelExtractionState modelState, RecommendationState recommendationState) {
        if (textExtractionState == null || word == null) {
            return;
        }

        var sameLemmaTypes = CommonUtilities.getSimilarTypes(word, modelState);
        if (!sameLemmaTypes.isEmpty()) {
            tss.addNounMapping(word, MappingKind.TYPE, this, this.probability);

            var typeMappings = textExtractionState.getMappingsThatCouldBeOfKind(word, MappingKind.TYPE);
            var nortMappings = textExtractionState.getMappingsThatCouldBeMultipleKinds(word.getNextWord(), MappingKind.NAME, MappingKind.TYPE);

            CommonUtilities.addRecommendedInstancesFromNounMappings(sameLemmaTypes, nortMappings, typeMappings, recommendationState, this, this.probability);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // handle additional config
    }

}
