/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner;

import java.io.Serial;
import java.util.EnumMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionStates;

public class NerConnectionStatesImpl implements NerConnectionStates {

    @Serial
    private static final long serialVersionUID = 439228649106802974L;
    private final EnumMap<Metamodel, NerConnectionStateImpl> connectionStates;

    private NerConnectionStatesImpl() {
        connectionStates = new EnumMap<>(Metamodel.class);
    }

    public static NerConnectionStatesImpl build(Metamodel[] metamodels) {
        var nerConnectionStates = new NerConnectionStatesImpl();
        for (Metamodel mm : metamodels) {
            NerConnectionStateImpl state = new NerConnectionStateImpl();
            state.setMetamodel(mm);
            nerConnectionStates.connectionStates.put(mm, state);
        }
        return nerConnectionStates;
    }

    @Override
    public NerConnectionStateImpl getNerConnectionState(Metamodel metamodel) {
        return connectionStates.get(metamodel);
    }

}
