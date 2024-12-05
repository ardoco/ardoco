/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.id.types;

import java.util.Locale;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.legacy.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.TextInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

public class NameInconsistency implements TextInconsistency {

    private static final String INCONSISTENCY_TYPE_NAME = "NameInconsistency";

    private final ModelInstance modelInstance;
    private final Word word;
    private final int sentenceNo;

    public NameInconsistency(ModelInstance modelInstance, Word word) {
        this.modelInstance = modelInstance;
        this.word = word;
        sentenceNo = word.getSentenceNo() + 1;
    }

    @Override
    public String getReason() {
        String textOccurrence = word.getText();
        String modelOccurrence = modelInstance.getFullName();
        String uid = modelInstance.getUid();
        return String.format(Locale.US, "Inconsistent naming in trace link between textual occurence \"%s\" and model element \"%s\" (%s)", textOccurrence,
                modelOccurrence, uid);
    }

    @Override
    public int getSentenceNumber() {
        return sentenceNo;
    }

    @Override
    public ImmutableList<String[]> toFileOutput() {
        MutableList<String[]> returnList = Lists.mutable.empty();
        var modelUid = modelInstance.getUid();
        returnList.add(new String[] { getType(), Integer.toString(sentenceNo), word.getText(), modelInstance.getFullName(), modelUid });
        return returnList.toImmutable();
    }

    @Override
    public String getType() {
        return INCONSISTENCY_TYPE_NAME;
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelInstance, word);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NameInconsistency other)) {
            return false;
        }
        return Objects.equals(modelInstance, other.modelInstance) && Objects.equals(word, other.word);
    }

}
