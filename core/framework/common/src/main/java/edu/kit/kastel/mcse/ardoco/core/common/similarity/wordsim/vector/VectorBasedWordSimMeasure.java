/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.vector;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimMeasure;

/**
 * A vector based word similarity measure uses vector embeddings of words to compare their similarity. To get vector
 * embeddings of passed words, a {@link WordVectorDataSource} is required. Instances of this class additionally manage a
 * cache to improve lookup speeds.
 */
@Deterministic
public abstract class VectorBasedWordSimMeasure implements WordSimMeasure {

    private static final float[] ZERO_VECTOR = {};
    private final Map<String, float[]> vectorCache = new LinkedHashMap<>();

    protected abstract WordVectorDataSource getVectorDataSource();

    /**
     * Compares the two given words by computing the cosine similarity between their respective vector representations.
     * If the vector representation for one of the words is not found, a similarity score of {@code 0.0} will be
     * returned.
     *
     * @param firstWord  the first word
     * @param secondWord the second word
     * @return returns the similarity score between the two words, between 0.0 and 1.0 (inclusive)
     * @throws RetrieveVectorException if an error occurs while retrieving the word vectors
     */
    public double compareVectors(String firstWord, String secondWord) throws RetrieveVectorException {
        Objects.requireNonNull(firstWord);
        Objects.requireNonNull(secondWord);

        if (firstWord.equals(secondWord)) {
            return 1.0;
        }

        float[] firstVec = this.getVectorFromCacheOrDatabase(firstWord);

        if (VectorUtils.isZero(firstVec)) {
            return 0.0; // no vector representation for the first word
        }

        float[] secondVec = this.getVectorFromCacheOrDatabase(secondWord);

        if (VectorUtils.isZero(secondVec)) {
            return 0.0; // no vector representation for the second word
        }

        return VectorUtils.cosineSimilarity(firstVec, secondVec);
    }

    private float[] getVectorFromCacheOrDatabase(String word) throws RetrieveVectorException {
        float[] vector = this.vectorCache.getOrDefault(word, null);

        if (vector == null) {
            vector = this.getVectorDataSource().getWordVector(word).orElse(ZERO_VECTOR);
            this.vectorCache.put(word, vector);
        }

        return vector;
    }

}
