/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction.informants;

import java.util.SortedMap;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * The analyzer classifies nouns.
 */
public class NounInformant extends Informant {
    @Configurable
    private double nameOrTypeWeight = 0.5;

    @Configurable
    private double probability = 0.2;

    /**
     * Prototype constructor.
     *
     * @param data the {@link DataRepository}
     */
    public NounInformant(DataRepository data) {
        super(NounInformant.class.getSimpleName(), data);
    }

    @Override
    public void process() {
        ImmutableList<Word> words = DataRepositoryHelper.getAnnotatedText(getDataRepository()).words();
        var textState = DataRepositoryHelper.getTextState(getDataRepository());
        for (var word : words) {
            var text = word.getText();
            if (text.length() > 1 && Character.isLetter(text.charAt(0))) {
                findSingleNouns(textState, word);
            }
        }
    }

    /**
     * Finds all nouns and adds them as name-or-type mappings (and types) to the text extraction state.
     */
    private void findSingleNouns(TextState textState, Word word) {
        var pos = word.getPosTag();
        if (POSTag.NOUN_PROPER_SINGULAR == pos || POSTag.NOUN == pos || POSTag.NOUN_PROPER_PLURAL == pos) {
            textState.addNounMapping(word, MappingKind.NAME, this, probability * nameOrTypeWeight);
            textState.addNounMapping(word, MappingKind.TYPE, this, probability * nameOrTypeWeight);
        }
        if (POSTag.NOUN_PLURAL == pos) {
            textState.addNounMapping(word, MappingKind.TYPE, this, probability);
        }

    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // empty
    }
}
