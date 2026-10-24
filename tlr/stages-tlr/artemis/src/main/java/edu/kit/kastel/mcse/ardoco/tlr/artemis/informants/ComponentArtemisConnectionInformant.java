/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.artemis.informants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

/**
 * Artemis connection informant that links named architecture entities from the documentation to component model entities.
 * <p>
 * The informant performs matching in multiple stages. It first applies strong name-based similarity, then weaker token-based similarity, and finally
 * embedding-based semantic similarity for entities that could not be linked by the previous steps. Created trace links are stored in the Artemis connection
 * state, while entities without a match are retained as unlinked named architecture entities.
 * </p>
 */
public class ComponentArtemisConnectionInformant extends ArtemisConnectionInformant {

    public ComponentArtemisConnectionInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(dataRepository, strategy);
    }

    @Override
    protected void process() {
        var similarityUtils = SimilarityUtils.getInstance();

        var state = DataRepositoryHelper.getArtemisConnectionStates(dataRepository).getState(strategy.getTarget());
        var modelEndpoints = DataRepositoryHelper.getModelStatesData(dataRepository).getModel(strategy.getMetamodel()).getEndpoints();

        var namedEntities = state.getNamedEntities();
        var unlinkedNamedEntities = Lists.mutable.withAll(namedEntities);
        MutableList<NamedArchitectureEntity> matchedNamedEntities = Lists.mutable.empty();
        List<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks = new ArrayList<>();

        for (var namedEntity : namedEntities) {
            for (var modelEndpoint : modelEndpoints) {
                if (areStronglySimilar(similarityUtils, namedEntity, modelEndpoint)) {
                    traceLinks.addAll(createTraceLinks(namedEntity, modelEndpoint));
                    matchedNamedEntities.add(namedEntity);
                }
            }
        }
        unlinkedNamedEntities.removeAll(matchedNamedEntities);
        matchedNamedEntities = Lists.mutable.empty();

        for (var namedEntity : unlinkedNamedEntities) {
            for (var modelEndpoint : modelEndpoints) {
                if (areWeaklySimilar(similarityUtils, namedEntity, modelEndpoint)) {
                    traceLinks.addAll(createTraceLinks(namedEntity, modelEndpoint));
                    matchedNamedEntities.add(namedEntity);
                }
            }
        }
        unlinkedNamedEntities.removeAll(matchedNamedEntities);
        matchedNamedEntities = Lists.mutable.empty();

        for (var namedEntity : unlinkedNamedEntities) {
            logger.debug("Trying to match using embeddings for the NAE: {}", namedEntity.getName());
            var namedEntityEmbeddings = getNamedArchitectureEntityEmbeddings(namedEntity);

            for (var modelEndpoint : modelEndpoints) {
                var modelEndpointEmbeddings = getModelEndpointEmbeddings(modelEndpoint);
                if (embeddingsAreSimilar(namedEntityEmbeddings, modelEndpointEmbeddings)) {
                    logger.debug("^ similarity for {} <-> {}", namedEntity.getName(), modelEndpoint.getName());
                    traceLinks.addAll(createTraceLinks(namedEntity, modelEndpoint));
                    matchedNamedEntities.add(namedEntity);
                }
            }
        }
        unlinkedNamedEntities.removeAll(matchedNamedEntities);

        state.addTraceLinks(Lists.mutable.ofAll(traceLinks));
        state.addUnlinkedNamedEntities(unlinkedNamedEntities);
    }
}
