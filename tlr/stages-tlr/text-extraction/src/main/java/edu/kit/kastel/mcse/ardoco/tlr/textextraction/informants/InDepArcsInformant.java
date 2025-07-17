/* Licensed under MIT 2021-2025. */
package edu.kit.kastel.mcse.ardoco.tlr.textextraction.informants;

import java.util.SortedMap;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.POSTag;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

/**
 * The analyzer examines the incoming dependency arcs of the current node.
 */
public class InDepArcsInformant extends TextExtractionInformant {

    @Configurable
    private final double nameOrTypeWeight = 0.5;

    @Configurable
    private final double probability = 1.0;

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
        for (var word : DataRepositoryHelper.getAnnotatedText(this.getDataRepository()).words()) {
            this.exec(word);
        }
    }

    private void exec(Word word) {
        var nodeValue = word.getText();
        if (nodeValue.length() == 1 && !Character.isLetter(nodeValue.charAt(0))) {
            return;
        }
        this.examineIncomingDepArcs(word);
    }

    /**
     * Examines the incoming dependency arcs.
     */
    private void examineIncomingDepArcs(Word word) {

        var incomingDepArcs = getIncomingDependencyTags(word);

        for (DependencyTag depTag : incomingDepArcs) {
            if (hasNameOrTypeDependencies(depTag)) {
                this.getTextStateStrategy().addNounMapping(word, MappingKind.NAME, this, this.probability * this.nameOrTypeWeight);
                this.getTextStateStrategy().addNounMapping(word, MappingKind.TYPE, this, this.probability * this.nameOrTypeWeight);
            } else if (hasTypeOrNameOrTypeDependencies(depTag)) {
                if (hasIndirectDeterminerAsPreWord(word)) {
                    this.getTextStateStrategy().addNounMapping(word, MappingKind.TYPE, this, this.probability);
                }

                this.getTextStateStrategy().addNounMapping(word, MappingKind.NAME, this, this.probability * this.nameOrTypeWeight);
                this.getTextStateStrategy().addNounMapping(word, MappingKind.TYPE, this, this.probability * this.nameOrTypeWeight);
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

    /**
     * Checks for indirect determiner as previous word.
     *
     * @param word the word
     * @return true, if found
     */
    private boolean hasIndirectDeterminerAsPreWord(Word word) {
        return hasDeterminerAsPreWord(word) && ("a".equalsIgnoreCase(word.getText()) || "an".equalsIgnoreCase(word.getText()));
    }

    /**
     * Checks for determiner as previous word.
     *
     * @param word the word
     * @return true, if found
     */
    private boolean hasDeterminerAsPreWord(Word word) {

        Word preWord = word.getPreWord();
        if (preWord == null) {
            return false;
        }

        var prePosTag = preWord.getPosTag();
        return POSTag.DETERMINER == prePosTag;

    }

    /**
     * Gets the incoming dependency tags.
     *
     * @param word the word
     * @return the incoming dependency tags
     */
    private ImmutableList<DependencyTag> getIncomingDependencyTags(Word word) {
        return Lists.immutable.with(DependencyTag.values()).select(d -> !word.getIncomingDependencyWordsWithType(d).isEmpty());
    }
}
