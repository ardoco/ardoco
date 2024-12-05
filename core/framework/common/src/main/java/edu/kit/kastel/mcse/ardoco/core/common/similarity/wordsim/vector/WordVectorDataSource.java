/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.vector;

import java.util.Optional;

/**
 * A data source that provides vector representations for words.
 */
public interface WordVectorDataSource {

    /**
     * Attempts to retrieve the vector representation for the given word.
     *
     * @param word the word
     * @return the vector representation of that word, or {@link Optional#empty()} if no vector representation for the
     *         given word exists
     * @throws RetrieveVectorException if an error occurs while trying to retrieve the vector
     */
    Optional<float[]> getWordVector(String word) throws RetrieveVectorException;

}
