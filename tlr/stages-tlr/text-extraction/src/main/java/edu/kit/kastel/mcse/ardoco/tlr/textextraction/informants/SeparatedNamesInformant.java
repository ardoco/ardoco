/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction.informants;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * This analyzer classifies all nodes, containing separators, as names and adds them as mappings to the current text
 * extraction state.
 */

public class SeparatedNamesInformant extends Informant {

    @Configurable
    private double probability = 0.8;

    /**
     * Prototype constructor.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public SeparatedNamesInformant(DataRepository dataRepository) {
        super(SeparatedNamesInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var textState = DataRepositoryHelper.getTextState(getDataRepository());
        for (var word : DataRepositoryHelper.getAnnotatedText(getDataRepository()).words()) {
            exec(textState, word);
        }
    }

    /***
     * Checks if Node Value contains separator. If true, it is split and added separately to the names of the text
     * extraction state.
     */
    private void exec(TextState textState, Word word) {
        checkForSeparatedNode(textState, word);
    }

    /**
     * Checks if Node Value contains separator. If true, it is split and added separately to the names of the text
     * extraction state.
     */
    private void checkForSeparatedNode(TextState textState, Word word) {
        if (word.getPosTag() != POSTag.FOREIGN_WORD && CommonUtilities.containsSeparator(word.getText())) {
            textState.addNounMapping(word, MappingKind.NAME, this, probability);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // emtpy
    }

}
