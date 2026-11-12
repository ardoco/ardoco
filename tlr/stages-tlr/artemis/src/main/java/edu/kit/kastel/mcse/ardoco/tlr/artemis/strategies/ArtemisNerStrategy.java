/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.ArtemisTarget;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.Prompt;

/**
 * Defines a strategy for Artemis named entity recognition.
 * <p>
 * Each strategy specifies how prompts are created for a particular named entity type and which metamodel the recognized entities belong to. The strategy also
 * provides a derived {@link ArtemisTarget}, which is used by later Artemis pipeline stages to identify the combination of metamodel and entity type handled by
 * this strategy.
 */
public interface ArtemisNerStrategy {

    String PROMPT_RESOURCE_ROOT = "/artemis/prompts/";

    /**
     * Creates the prompt used to identify named entities for this strategy.
     *
     * @param dataRepository the data repository containing pipeline data that may be used to create the prompt
     * @return the prompt for named entity recognition
     */
    Prompt createPrompt(DataRepository dataRepository);

    /**
     * Returns the metamodel this strategy targets.
     *
     * @return the target metamodel
     */
    Metamodel getMetamodel();

    /**
     * Returns the named entity type this strategy identifies.
     *
     * @return the named entity type
     */
    NamedEntityType getNamedEntityType();

    /**
     * Returns the Artemis target represented by this strategy.
     * <p>
     * The target is derived from the strategy's metamodel and named entity type.
     *
     * @return the Artemis target for this strategy
     */
    default ArtemisTarget getTarget() {
        return new ArtemisTarget(getMetamodel(), getNamedEntityType());
    }

    /**
     * Returns a stable identifier for this strategy.
     * <p>
     * By default, the identifier consists of the named entity type followed by {@code Artemis}.
     *
     * @return the strategy identifier
     */
    default String getId() {
        return getNamedEntityType().name() + "Artemis";
    }

    /**
     * Reads a prompt from the classpath resources.
     *
     * @param resourceName the resource file name below {@link #PROMPT_RESOURCE_ROOT}
     * @return the prompt text
     */
    default String readPromptResource(String resourceName) {
        try (var inputStream = Objects.requireNonNull(getClass().getResourceAsStream(PROMPT_RESOURCE_ROOT + resourceName),
                "Prompt resource not found: " + PROMPT_RESOURCE_ROOT + resourceName)) {
            return CommonUtilities.readInputText(inputStream);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Could not read prompt resource: " + PROMPT_RESOURCE_ROOT + resourceName, e);
        }
    }
}
