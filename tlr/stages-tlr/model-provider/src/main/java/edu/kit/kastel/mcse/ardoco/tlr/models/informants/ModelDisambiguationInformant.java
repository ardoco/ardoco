/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.informants;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class ModelDisambiguationInformant extends Informant {
    public ModelDisambiguationInformant(DataRepository dataRepository) {
        super(ModelDisambiguationInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        for (var modelId : modelStates.modelIds()) {
            var modelExtractionState = modelStates.getModelExtractionState(modelId);
            var instances = modelExtractionState.getInstances();
            for (var instance : instances) {
                var names = instance.getNameParts();
                for (var name : names) {
                    var abbreviations = AbbreviationDisambiguationHelper.getAbbreviationCandidates(name);
                    for (var abbreviation : abbreviations) {
                        var disambiguation = AbbreviationDisambiguationHelper.disambiguate(abbreviation);
                        AbbreviationDisambiguationHelper.addTransient(new Disambiguation(abbreviation, disambiguation.toArray(new String[0])));
                    }
                }
            }
        }
    }
}
