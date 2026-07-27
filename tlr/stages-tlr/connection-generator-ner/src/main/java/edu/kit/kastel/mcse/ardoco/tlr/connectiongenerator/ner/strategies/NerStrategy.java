package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;

/**
 * Defines the configuration and behavior of named entity recognition (NER) for a specific ArTEMiS traceability approach.
 * <p>
 * A strategy specifies the prompt used by the named entity recognizer and the metamodel for which the recognition is applicable. Implementations may optionally
 * provide additional known entities from the available models to support the recognition process.
 * </p>
 * <p>
 * Different ArTEMiS variants (e.g., class-decision, component-decision traceability, ...) provide different implementations of this strategy to adapt the NER
 * process to their specific target entities.
 * </p>
 */
public interface NerStrategy {
    Logger logger = LoggerFactory.getLogger(NerStrategy.class);

    /**
     * Returns the prompt used for NER.
     *
     * @param dataRepository the data repository
     * @return the prompt defining the recognition task and expected output format
     */
    Prompt getPrompt(DataRepository dataRepository);

    /**
     * Returns the metamodel supported by this NER strategy.
     *
     * @return the metamodel for which this strategy performs named entity recognition
     */
    Metamodel getMetamodel();
}
