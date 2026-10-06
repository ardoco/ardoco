/* Licensed under MIT 2021-2026. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
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
     * Load-on-resume for ConnectionState instance links (RI → Architecture / RI → Code).
     * Requires RecommendationStates and models already in memory / Neo4j.
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
        SortedMap<String, RecommendedInstance> risById = new TreeMap<>();
        SortedMap<String, ArchitectureItem> archById = new TreeMap<>();
        SortedMap<String, CodeItem> codeById = new TreeMap<>();
        for (Metamodel metamodel : activeMetamodels) {
            var recommendationState = recommendationStates.getRecommendationState(metamodel);
            if (recommendationState == null) {
                continue;
            }
            for (RecommendedInstance ri : recommendationState.getRecommendedInstances()) {
                risById.put(ri.getId(), ri);
            }
            Model model = modelStates.getModel(metamodel);
            if (model instanceof ArchitectureModel architectureModel) {
                collectArchitectureItems(architectureModel, archById);
            }
            if (model instanceof CodeModel codeModel) {
                for (var item : codeModel.getEndpoints()) {
                    if (item instanceof CodeItem codeItem) {
                        codeById.put(codeItem.getId(), codeItem);
                    }
                }
            }
        }

        Collection<RecommendationModelTraceLink> loadedArch = PersistenceBridge.callQuietly("loadRecommendationModelTraceLinks",
                () -> handler.loadRecommendationModelTraceLinks(risById, archById), List.of());
        Collection<RecommendationModelTraceLink> loadedCode = PersistenceBridge.callQuietly("loadRecommendationCodeTraceLinks",
                () -> handler.loadRecommendationCodeTraceLinks(risById, codeById), List.of());

        int total = 0;
        for (RecommendationModelTraceLink link : loadedArch) {
            total += hydrateOne(connectionStates, activeMetamodels, recommendationStates, link);
        }
        for (RecommendationModelTraceLink link : loadedCode) {
            total += hydrateOne(connectionStates, activeMetamodels, recommendationStates, link);
        }
        if (total > 0) {
            logger.info("Hydrated {} RecommendationModelTraceLinks into ConnectionState (resume)", total);
        }
    }

    private static int hydrateOne(ConnectionStatesImpl connectionStates, Collection<Metamodel> activeMetamodels, RecommendationStates recommendationStates,
            RecommendationModelTraceLink link) {
        Metamodel metamodel = findMetamodelForLink(activeMetamodels, recommendationStates, link);
        if (metamodel == null) {
            return 0;
        }
        ConnectionStateImpl connectionState = connectionStates.getConnectionState(metamodel);
        if (connectionState == null) {
            logger.warn("No ConnectionState bucket for metamodel {} when hydrating trace link", metamodel);
            return 0;
        }
        connectionState.hydrateInstanceLink(link);
        return 1;
    }

    private static Metamodel findMetamodelForLink(Collection<Metamodel> activeMetamodels, RecommendationStates recommendationStates,
            RecommendationModelTraceLink link) {
        RecommendedInstance recommendedInstance = link.getFirstEndpoint();
        if (recommendedInstance.getMetamodel() != null) {
            return recommendedInstance.getMetamodel();
        }
        String riId = recommendedInstance.getId();
        for (Metamodel metamodel : activeMetamodels) {
            var recommendationState = recommendationStates.getRecommendationState(metamodel);
            if (recommendationState == null) {
                continue;
            }
            boolean found = recommendationState.getRecommendedInstances().anySatisfy(ri -> ri.getId().equals(riId));
            if (found) {
                return metamodel;
            }
        }
        logger.warn("Could not resolve metamodel for RecommendedInstance id {} when hydrating trace link; skipping", riId);
        return null;
    }

    private static void collectArchitectureItems(ArchitectureModel architectureModel, SortedMap<String, ArchitectureItem> archById) {
        for (ArchitectureItem endpoint : architectureModel.getEndpoints()) {
            collectArchitectureItem(endpoint, archById);
        }
    }

    private static void collectArchitectureItem(ArchitectureItem item, SortedMap<String, ArchitectureItem> archById) {
        archById.put(item.getId(), item);
        if (item instanceof ArchitectureComponent component) {
            for (ArchitectureComponent subcomponent : component.getSubcomponents()) {
                collectArchitectureItem(subcomponent, archById);
            }
            for (ArchitectureInterface provided : component.getProvidedInterfaces()) {
                archById.put(provided.getId(), provided);
            }
            for (ArchitectureInterface required : component.getRequiredInterfaces()) {
                archById.put(required.getId(), required);
            }
        }
    }
}
