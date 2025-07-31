/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner;

import java.io.Serial;
import java.util.EnumMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionStates;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.llm.LlmSettings;

public class NerConnectionStatesImpl implements NerConnectionStates {

    @Serial
    private static final long serialVersionUID = 439228649106802974L;
    private final EnumMap<Metamodel, NerConnectionStateImpl> connectionStates;
    private LlmSettings llmSettings;

    private NerConnectionStatesImpl() {
        connectionStates = new EnumMap<>(Metamodel.class);
    }

    public static NerConnectionStatesImpl build(Metamodel[] metamodels) {
        logger.info("Building connection states for {} metamodels", metamodels.length);
        var nerConnectionStates = new NerConnectionStatesImpl();
        for (Metamodel mm : metamodels) {
            nerConnectionStates.connectionStates.put(mm, new NerConnectionStateImpl());
        }
        return nerConnectionStates;
    }

    @Override
    public NerConnectionStateImpl getNerConnectionState(Metamodel metamodel) {
        return connectionStates.get(metamodel);
    }

    public LlmSettings getLlmSettings() {
        return this.llmSettings;
    }

    public void setLlmSettings(LlmSettings llmSettings) {
        this.llmSettings = llmSettings;
    }
}
