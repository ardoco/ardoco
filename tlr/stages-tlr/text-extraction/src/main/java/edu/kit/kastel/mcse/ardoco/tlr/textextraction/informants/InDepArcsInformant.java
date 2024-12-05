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
 * The analyzer examines the incoming dependency arcs of the current node.
 */
public class InDepArcsInformant extends Informant {

    @Configurable
    private double nameOrTypeWeight = 0.5;

    @Configurable
    private double probability = 1.0;

    /**
     * Prototype constructor.
     *
     * @param data the {@link DataRepository}
     */
    public InDepArcsInformant(DataRepository data) {
        super(InDepArcsInformant.class.getSimpleName(), data);
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
        examineIncomingDepArcs(textState, word);
    }

    /**
     * Examines the incoming dependency arcs from the PARSE graph.
     */
    private void examineIncomingDepArcs(TextState textState, Word word) {

        var incomingDepArcs = WordHelper.getIncomingDependencyTags(word);

        for (DependencyTag depTag : incomingDepArcs) {
            if (hasNameOrTypeDependencies(depTag)) {
                textState.addNounMapping(word, MappingKind.NAME, this, probability * nameOrTypeWeight);
                textState.addNounMapping(word, MappingKind.TYPE, this, probability * nameOrTypeWeight);
            } else if (hasTypeOrNameOrTypeDependencies(depTag)) {
                if (WordHelper.hasIndirectDeterminerAsPreWord(word)) {
                    textState.addNounMapping(word, MappingKind.TYPE, this, probability);
                }

                textState.addNounMapping(word, MappingKind.NAME, this, probability * nameOrTypeWeight);
                textState.addNounMapping(word, MappingKind.TYPE, this, probability * nameOrTypeWeight);
            }
        }
    }

    private static boolean hasTypeOrNameOrTypeDependencies(DependencyTag depTag) {
        var hasObjectDependencies = DependencyTag.OBJ == depTag || DependencyTag.IOBJ == depTag || DependencyTag.POBJ == depTag;
        return hasObjectDependencies || DependencyTag.NMOD == depTag || DependencyTag.NSUBJPASS == depTag;
    }

    private static boolean hasNameOrTypeDependencies(DependencyTag depTag) {
        return DependencyTag.APPOS == depTag || DependencyTag.NSUBJ == depTag || DependencyTag.POSS == depTag;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // emtpy
    }
}
