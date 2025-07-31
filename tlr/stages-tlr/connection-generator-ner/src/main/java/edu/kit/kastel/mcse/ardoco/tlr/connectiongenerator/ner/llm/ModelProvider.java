/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.llm;

public enum ModelProvider {

    OLLAMA, OPEN_AI;

    public edu.kit.kastel.mcse.ardoco.naer.util.ModelProvider getNaerModelProvider() {
        return switch (this) {
            case OLLAMA -> edu.kit.kastel.mcse.ardoco.naer.util.ModelProvider.OLLAMA;
            case OPEN_AI -> edu.kit.kastel.mcse.ardoco.naer.util.ModelProvider.OPEN_AI;
        };
    }
}
