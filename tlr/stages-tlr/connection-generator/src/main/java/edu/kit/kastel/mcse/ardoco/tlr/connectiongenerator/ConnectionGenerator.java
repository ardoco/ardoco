/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.RecommendationModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents.InitialConnectionAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents.InstanceConnectionAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents.ProjectNameFilterAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.agents.ReferenceAgent;

/**
 * The ModelConnectionAgent runs different analyzers and solvers. This agent creates recommendations as well as matchings between text and model. The order is
 * important: All connections should run after the recommendations have been made.
 */
public class ConnectionGenerator extends AbstractExecutionStage {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionGenerator.class);

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
    public static ConnectionGenerator get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        var connectionGenerator = new ConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(additionalConfigs);
        return connectionGenerator;
    }

    @Override
    protected void initializeState() {
        var dataRepository = this.getDataRepository();
        var activeMetamodels = dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow().getMetamodels();
        ConnectionStatesImpl connectionStates = ConnectionStatesImpl.build(activeMetamodels.toArray(Metamodel[]::new));
        hydrateFromPersistenceIfPresent(dataRepository, connectionStates, activeMetamodels);
        getDataRepository().addData(ConnectionStates.ID, connectionStates);
    }

    /**
     * Load-on-resume for ConnectionState instance links (RI → Architecture).
     * Requires RecommendationStates and architecture models already in memory / Neo4j.
     */
    private static void hydrateFromPersistenceIfPresent(DataRepository dataRepository, ConnectionStatesImpl connectionStates,
            Collection<Metamodel> activeMetamodels) {
        if (!PersistenceBridge.isAvailable()) {
            return;
        }
        var handler = PersistenceBridge.getHandler();
        if (handler == null) {
            return;
        }
        Boolean hasLinks = PersistenceBridge.callQuietly("hasRecommendationModelTraceLinks", handler::hasRecommendationModelTraceLinks, Boolean.FALSE);
        if (!Boolean.TRUE.equals(hasLinks)) {
            return;
        }
        if (dataRepository.getData(RecommendationStates.ID, RecommendationStates.class).isEmpty()) {
            logger.warn("Cannot load ConnectionState instance links: RecommendationStates not available");
            return;
        }

        RecommendationStates recommendationStates = dataRepository.getData(RecommendationStates.ID, RecommendationStates.class).orElseThrow();
        ModelStates modelStates = dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow();

        Map<String, RecommendedInstance> risById = new HashMap<>();
        Map<String, ArchitectureItem> archById = new HashMap<>();
        for (Metamodel metamodel : activeMetamodels) {
            for (RecommendedInstance ri : recommendationStates.getRecommendationState(metamodel).getRecommendedInstances()) {
                risById.put(ri.getId(), ri);
            }
            Model model = modelStates.getModel(metamodel);
            if (model instanceof ArchitectureModel architectureModel) {
                for (var item : architectureModel.getContent()) {
                    if (item instanceof ArchitectureItem architectureItem) {
                        archById.put(architectureItem.getId(), architectureItem);
                    }
                }
            }
        }

        Collection<RecommendationModelTraceLink> loaded = PersistenceBridge.callQuietly("loadRecommendationModelTraceLinks",
                () -> handler.loadRecommendationModelTraceLinks(risById, archById), List.of());
        int total = 0;
        for (RecommendationModelTraceLink link : loaded) {
            Metamodel metamodel = findMetamodelForLink(activeMetamodels, recommendationStates, link);
            if (metamodel == null) {
                continue;
            }
            connectionStates.getConnectionState(metamodel).hydrateInstanceLink(link);
            total++;
        }
        if (total > 0) {
            logger.info("Hydrated {} RecommendationModelTraceLinks into ConnectionState (resume)", total);
        }
    }

    private static Metamodel findMetamodelForLink(Collection<Metamodel> activeMetamodels, RecommendationStates recommendationStates,
            RecommendationModelTraceLink link) {
        String riId = link.getFirstEndpoint().getId();
        for (Metamodel metamodel : activeMetamodels) {
            boolean found = recommendationStates.getRecommendationState(metamodel)
                    .getRecommendedInstances()
                    .anySatisfy(ri -> ri.getId().equals(riId));
            if (found) {
                return metamodel;
            }
        }
        return activeMetamodels.stream().findFirst().orElse(null);
    }
}
