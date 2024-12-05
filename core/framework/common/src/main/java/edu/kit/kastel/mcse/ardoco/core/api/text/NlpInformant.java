/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.text;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * The Interface ITextConnector defines a provider for annotated texts.
 */
public abstract class NlpInformant extends Informant {

    protected NlpInformant(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    /**
     * Gets the annotated text.
     *
     * @return the annotated text
     */
    public abstract Text getAnnotatedText();

    /**
     * Gets the annotated text with the given name
     *
     * @param textName name of the text to retrieve
     * @return the annotated text with the given name
     */
    public abstract Text getAnnotatedText(String textName);
}
