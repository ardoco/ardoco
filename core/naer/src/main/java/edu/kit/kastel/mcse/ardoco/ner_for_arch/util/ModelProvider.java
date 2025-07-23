package edu.kit.kastel.mcse.ardoco.ner_for_arch.util;

/**
 * Enum representing different providers of chat models supported by the application.
 * <p>
 * This enum is used by {@link ChatModelFactory} to determine which type of chat model to create.
 * </p>
 */
public enum ModelProvider {
    /**
     * Virtual Design Lab Server provider (from KIT SDQ).
     */
    VDL,

    /**
     * OpenAI provider.
     */
    OPEN_AI,

    /**
     * Local provider for locally hosted models (not yet implemented).
     */
    LOCAL
}
