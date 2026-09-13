/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.agents.NerAgent;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.agents.NerConnectionAgent;
import edu.kit.kastel.mcse.ardoco.tlr.models.informants.LargeLanguageModel;

public class NerConnectionGenerator extends AbstractExecutionStage {
    private static final Logger logger = LoggerFactory.getLogger(NerConnectionGenerator.class);
    protected static final String LOGGING_SETUP_DEBUG = "org.slf4j.simpleLogger.log.edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner";

    public NerConnectionGenerator(DataRepository dataRepository, LargeLanguageModel llm) {
        super(List.of(new NerAgent(dataRepository, llm), new NerConnectionAgent(dataRepository)), NerConnectionGenerator.class.getSimpleName(), dataRepository);
    }

    public static NerConnectionGenerator get(ImmutableSortedMap<String, String> additionalConfigs, DataRepository dataRepository, LargeLanguageModel llm) {
        var connectionGenerator = new NerConnectionGenerator(dataRepository, llm);
        connectionGenerator.applyConfiguration(additionalConfigs);
        return connectionGenerator;
    }

    @Override
    protected void initializeState() {
        var activeMetamodels = this.getDataRepository().getData(ModelStates.ID, ModelStates.class).orElseThrow().getMetamodels();
        var connectionStates = NerConnectionStatesImpl.build(activeMetamodels.toArray(Metamodel[]::new));
        hydrateFromPersistenceIfPresent(this.getDataRepository(), connectionStates, activeMetamodels);
        getDataRepository().addData(NerConnectionStates.ID, connectionStates);
    }

    private static void hydrateFromPersistenceIfPresent(DataRepository dataRepository, NerConnectionStatesImpl connectionStates,
            Collection<Metamodel> activeMetamodels) {
        if (!PersistenceBridge.shouldPersistNerConnection()) {
            return;
        }
        var handler = PersistenceBridge.getHandler();
        if (handler == null) {
            return;
        }
        Boolean hasEntities = PersistenceBridge.callQuietly("hasNerNamedArchitectureEntities", handler::hasNerNamedArchitectureEntities, Boolean.FALSE);
        if (!Boolean.TRUE.equals(hasEntities)) {
            return;
        }

        ModelStates modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        int totalEntities = 0;
        int totalLinks = 0;
        for (Metamodel metamodel : activeMetamodels) {
            NerConnectionStateImpl state = connectionStates.getNerConnectionState(metamodel);
            Collection<NamedArchitectureEntity> linked = PersistenceBridge.callQuietly("loadNamedArchitectureEntities",
                    () -> handler.loadNamedArchitectureEntities(metamodel, false), List.of());
            Collection<NamedArchitectureEntity> unlinked = PersistenceBridge.callQuietly("loadUnlinkedNamedArchitectureEntities",
                    () -> handler.loadNamedArchitectureEntities(metamodel, true), List.of());
            state.hydrateNamedEntities(linked);
            state.hydrateUnlinkedNamedEntities(unlinked);
            totalEntities += linked.size() + unlinked.size();

            SortedMap<String, NamedArchitectureEntityOccurrence> occurrencesById = new TreeMap<>();
            for (NamedArchitectureEntity entity : linked) {
                for (NamedArchitectureEntityOccurrence occ : entity.getOccurrences()) {
                    occurrencesById.put(occ.getId(), occ);
                }
            }
            for (NamedArchitectureEntity entity : unlinked) {
                for (NamedArchitectureEntityOccurrence occ : entity.getOccurrences()) {
                    occurrencesById.put(occ.getId(), occ);
                }
            }

            SortedMap<String, ModelEntity> modelEntitiesById = new TreeMap<>();
            Model model = modelStates.getModel(metamodel);
            if (model != null) {
                for (ModelEntity me : model.getEndpoints()) {
                    modelEntitiesById.put(me.getId(), me);
                }
            }

            Collection<NamedArchitectureEntityToModelTraceLink> links = PersistenceBridge.callQuietly("loadNerTraceLinks",
                    () -> handler.loadNerTraceLinks(metamodel, occurrencesById, modelEntitiesById), List.of());
            for (NamedArchitectureEntityToModelTraceLink link : links) {
                state.hydrateTraceLink(link);
            }
            totalLinks += links.size();
        }
        if (totalEntities > 0 || totalLinks > 0) {
            logger.info("Hydrated {} NER entities and {} NER links from Neo4j (resume)", totalEntities, totalLinks);
        }
    }
}
