package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.strategies;

import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
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
    /**
     * Returns the prompt used for NER.
     *
     * @return the prompt defining the recognition task and expected output format
     */
    Prompt getPrompt();

    /**
     * Returns additional entities that can support the NER process.
     * <p>
     * The default implementation does not provide any additional entities. Implementations may override this method to supply known entities extracted from
     * available models, allowing the recognizer to use model information as additional context.
     * </p>
     *
     * @param dataRepository the data repository containing the current pipeline data
     * @return a mapping from entity types to possible entity names, or an empty map if no additional entities are provided
     */
    default Map<NamedEntityType, Set<String>> getPossibleEntities(DataRepository dataRepository) {
        return Map.of();
    }

    /**
     * Returns the metamodel supported by this NER strategy.
     *
     * @return the metamodel for which this strategy performs named entity recognition
     */
    Metamodel getMetamodel();
}
