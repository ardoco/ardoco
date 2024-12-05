/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction.informants;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.WordHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * The analyzer examines the outgoing arcs of the current node.
 */

public class OutDepArcsInformant extends Informant {

    @Configurable
    private double nameOrTypeWeight = 0.5;

    @Configurable
    private double probability = 0.8;

    /**
     * Prototype constructor.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public OutDepArcsInformant(DataRepository dataRepository) {
        super(OutDepArcsInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var textState = DataRepositoryHelper.getTextState(getDataRepository());
        for (var word : DataRepositoryHelper.getAnnotatedText(getDataRepository()).words()) {
            exec(textState, word);
        }
    }

    private void exec(TextState textState, Word word) {

        var nodeValue = word.getText();
        if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
            return;
        }
        examineOutgoingDepArcs(textState, word);
    }

    /**
     * Examines the outgoing dependencies of a node.
     */
    private void examineOutgoingDepArcs(TextState textState, Word word) {

        var outgoingDepArcs = WordHelper.getOutgoingDependencyTags(word);

        for (DependencyTag shortDepTag : outgoingDepArcs) {

            if (DependencyTag.AGENT == shortDepTag || DependencyTag.RCMOD == shortDepTag) {
                textState.addNounMapping(word, MappingKind.NAME, this, probability * nameOrTypeWeight);
                textState.addNounMapping(word, MappingKind.TYPE, this, probability * nameOrTypeWeight);
            } else if (DependencyTag.NUM == shortDepTag || DependencyTag.PREDET == shortDepTag) {
                textState.addNounMapping(word, MappingKind.TYPE, this, probability);
            }
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // emtpy
    }

}
