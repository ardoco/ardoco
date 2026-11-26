/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.core.textproviderjson.textobject;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

@Deterministic
public class PhraseImpl implements Phrase {
    private static final String PUNCTUATION_WITH_SPACE = "\\s+([.,;:?!])";
    private static final String BRACKETS_WITH_SPACE = "\\s+([()\\[\\]{}<>])";
    @Serial
    private static final long serialVersionUID = 5809331492733614205L;
    private final PhraseType type;
    private MutableList<Phrase> childPhrases;
    private MutableList<Word> nonPhraseWords;
    private @Nullable MutableList<Word> phraseWords;
    private @Nullable MutableList<Word> containedWords;
    private @Nullable MutableList<Phrase> subphrases;
    private @Nullable MutableSortedMap<Word, Integer> phraseVector;
    private int sentenceNo = -1;
    private @Nullable String text;

    public PhraseImpl(ImmutableList<Word> nonPhraseWords, PhraseType type, List<Phrase> childPhrases) {
        this.nonPhraseWords = nonPhraseWords == null ? Lists.mutable.empty() : nonPhraseWords.toList();
        this.type = type;
        this.childPhrases = Lists.mutable.ofAll(childPhrases);
    }

    @Override
    public synchronized int getSentenceNumber() {
        if (sentenceNo < 0) {
            sentenceNo = getContainedWords().get(0).getSentenceNumber();
        }
        return sentenceNo;
    }

    @Override
    public String getText() {
        var currentText = this.text;
        if (currentText == null) {
            MutableList<Word> wordList = getContainedWords().toList();
            wordList.sort(Comparator.comparingInt(Word::getPosition));
            List<String> wordText = wordList.collect(Word::getText);
            // Join string with spaces but remove spaces before punctuation and brackets
            currentText = String.join(" ", wordText).replaceAll(PUNCTUATION_WITH_SPACE, "$1").replaceAll(BRACKETS_WITH_SPACE, "$1");
            this.text = currentText;
        }
        return currentText;
    }

    @Override
    public PhraseType getPhraseType() {
        return this.type;
    }

    @Override
    public synchronized ImmutableList<Word> getContainedWords() {
        var currentContainedWords = containedWords;
        if (currentContainedWords == null) {
            var currentPhraseWords = phraseWords;
            if (currentPhraseWords == null) {
                List<Word> collectedWords = new ArrayList<>();
                for (Phrase subphrase : childPhrases) {
                    collectedWords.addAll(subphrase.getContainedWords().castToList());
                }
                currentPhraseWords = Lists.mutable.ofAll(collectedWords);
                this.phraseWords = currentPhraseWords;
            }

            currentContainedWords = Lists.mutable.ofAll(nonPhraseWords);
            currentContainedWords.addAllIterable(currentPhraseWords);
            currentContainedWords.sortThis(Comparator.comparingInt(Word::getPosition));
            containedWords = currentContainedWords;
        }

        return currentContainedWords.toImmutable();
    }

    @Override
    public synchronized ImmutableList<Phrase> getSubphrases() {
        var currentSubphrases = subphrases;
        if (currentSubphrases == null) {
            currentSubphrases = Lists.mutable.ofAll(childPhrases);
            for (Phrase childPhrase : childPhrases) {
                currentSubphrases.addAll(childPhrase.getSubphrases().toList());
            }
            subphrases = currentSubphrases;
        }
        return currentSubphrases.toImmutable();
    }

    @Override
    public boolean isSuperphraseOf(Phrase other) {
        MutableList<Phrase> subphrases = Lists.mutable.ofAll(this.getSubphrases());
        while (!subphrases.isEmpty()) {
            if (subphrases.contains(other)) {
                return true;
            }
            subphrases = getSubphrasesOfPhrases(subphrases);
        }
        return false;
    }

    private static MutableList<Phrase> getSubphrasesOfPhrases(MutableList<Phrase> subphrases) {
        MutableList<Phrase> subphrasesOfPhrases = Lists.mutable.empty();
        for (Phrase subphrase : subphrases) {
            subphrasesOfPhrases.addAll(subphrase.getSubphrases().castToList());
        }
        return subphrasesOfPhrases;
    }

    @Override
    public boolean isSubphraseOf(Phrase other) {
        MutableList<Phrase> subphrases = Lists.mutable.ofAll(other.getSubphrases());
        while (!subphrases.isEmpty()) {
            if (subphrases.contains(this)) {
                return true;
            }
            subphrases = getSubphrasesOfPhrases(subphrases);
        }
        return false;
    }

    @Override
    public synchronized ImmutableSortedMap<Word, Integer> getPhraseVector() {
        var currentPhraseVector = phraseVector;
        if (currentPhraseVector == null) {
            MutableSortedMap<Word, Integer> newPhraseVector = SortedMaps.mutable.empty();
            var grouped = getContainedWords().groupBy(Word::getText).toMap();
            grouped.forEach((key, value) -> newPhraseVector.put(value.getAny(), value.size()));
            phraseVector = newPhraseVector;
            currentPhraseVector = newPhraseVector;
        }
        return currentPhraseVector.toImmutable();
    }

    @Override
    public String toString() {
        return "Phrase{" + "text='" + getText() + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PhraseImpl phrase))
            return false;
        return Objects.equals(getContainedWords(), phrase.getContainedWords()) && Objects.equals(getText(), phrase.getText()) && type == phrase.type && Objects
                .equals(childPhrases, phrase.childPhrases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContainedWords(), getText(), type, childPhrases);
    }

    @Override
    public int compareTo(Phrase o) {
        return Comparator.comparing(Phrase::getSentenceNumber)
                .thenComparing(Phrase::getText)
                .thenComparing(Phrase::getPhraseType)
                .thenComparingInt(p -> p.getContainedWords().get(0).getPosition())
                .compare(this, o);
    }
}
