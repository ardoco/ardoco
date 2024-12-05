/* Licensed under MIT 2021-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator;

import java.util.List;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents.InitialConnectionAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents.InstanceConnectionAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents.ProjectNameFilterAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents.ReferenceAgent;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;

/**
 * The ModelConnectionAgent runs different analyzers and solvers. This agent creates recommendations as well as matchings between text and model. The order is
 * important: All connections should run after the recommendations have been made.
 */
public class ConnectionGenerator extends AbstractExecutionStage {

    /**
     * Create the module.
     *
     * @param dataRepository the {@link DataRepository}
     */
    public ConnectionGenerator(DataRepository dataRepository) {
        super(List.of(new InitialConnectionAgent(dataRepository), new ReferenceAgent(dataRepository), new ProjectNameFilterAgent(dataRepository),
                new InstanceConnectionAgent(dataRepository)), "ConnectionGenerator", dataRepository);
    }

    /**
     * Creates a {@link ConnectionGenerator} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of connectionGenerator
     */
    public static ConnectionGenerator get(SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var connectionGenerator = new ConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(additionalConfigs);
        return connectionGenerator;
    }

    @Override
    protected void initializeState() {
        var connectionStates = ConnectionStatesImpl.build();
        getDataRepository().addData(ConnectionStates.ID, connectionStates);
    }
}
