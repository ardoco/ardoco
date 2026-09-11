/* Licensed under MIT 2022-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator;

import java.io.Serial;
import java.util.EnumMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;

public class ConnectionStatesImpl implements ConnectionStates {
    @Serial
    private static final long serialVersionUID = 2578919199522815549L;
    private static final Logger logger = LoggerFactory.getLogger(ConnectionStatesImpl.class);
    private final EnumMap<Metamodel, ConnectionStateImpl> connectionStates;

    private ConnectionStatesImpl() {
        connectionStates = new EnumMap<>(Metamodel.class);
    }

    public static ConnectionStatesImpl build(Metamodel[] metamodels) {
        logger.info("Building connection states for {} metamodels", metamodels.length);
        var recStates = new ConnectionStatesImpl();
        for (Metamodel mm : metamodels) {
            recStates.connectionStates.put(mm, new ConnectionStateImpl());
        }
        return recStates;
    }

    @Override
    public ConnectionStateImpl getConnectionState(Metamodel metamodel) {
        return connectionStates.get(metamodel);
    }
}
