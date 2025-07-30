package edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner;

import edu.kit.kastel.mcse.ardoco.core.api.entity.TextEntity;

public class NamedArchitectureEntityOccurrence extends TextEntity {
    private final int sentenceNumber;

    public NamedArchitectureEntityOccurrence(String name, int sentenceNumber) {
        super(name, name + "-" + sentenceNumber);
        this.sentenceNumber = sentenceNumber;
    }

    public int getSentenceNumber() {
        return this.sentenceNumber;
    }

}
