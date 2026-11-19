/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.artemis.informants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

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
        List<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks = new ArrayList<>();

        var stronglyMatched = linkMatching(namedEntities, modelEndpoints, namedEntity -> modelEndpoint -> areStronglySimilar(similarityUtils, namedEntity,
                modelEndpoint), traceLinks);
        unlinkedNamedEntities.removeAll(stronglyMatched);

        var weaklyMatched = linkMatching(unlinkedNamedEntities, modelEndpoints, namedEntity -> modelEndpoint -> areWeaklySimilar(similarityUtils, namedEntity,
                modelEndpoint), traceLinks);
        unlinkedNamedEntities.removeAll(weaklyMatched);

        var embeddingMatched = linkMatching(unlinkedNamedEntities, modelEndpoints, this::embeddingMatcher, traceLinks);
        unlinkedNamedEntities.removeAll(embeddingMatched);

        state.addTraceLinks(Lists.mutable.ofAll(traceLinks));
        state.addUnlinkedNamedEntities(unlinkedNamedEntities);
    }

    private Predicate<ModelEntity> embeddingMatcher(NamedArchitectureEntity namedEntity) {
        logger.debug("Trying to match using embeddings for the NAE: {}", namedEntity.getName());
        var namedEntityEmbeddings = getNamedArchitectureEntityEmbeddings(namedEntity);

        return modelEndpoint -> {
            boolean similar = embeddingsAreSimilar(namedEntityEmbeddings, getModelEndpointEmbeddings(modelEndpoint));
            if (similar) {
                logger.debug("^ similarity for {} <-> {}", namedEntity.getName(), modelEndpoint.getName());
            }
            return similar;
        };
    }

    /**
     * Creates a trace link for every (entity, endpoint) pair accepted by the matcher and returns the entities that matched at least once (once per match).
     */
    private MutableList<NamedArchitectureEntity> linkMatching(Iterable<NamedArchitectureEntity> entities, List<? extends ModelEntity> modelEndpoints,
            Function<NamedArchitectureEntity, Predicate<ModelEntity>> matcherFactory,
            List<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks) {
        MutableList<NamedArchitectureEntity> matched = Lists.mutable.empty();

        for (var namedEntity : entities) {
            Predicate<ModelEntity> matcher = matcherFactory.apply(namedEntity);
            for (var modelEndpoint : modelEndpoints) {
                if (matcher.test(modelEndpoint)) {
                    traceLinks.addAll(createTraceLinks(namedEntity, modelEndpoint));
                    matched.add(namedEntity);
                }
            }
        }

        return matched;
    }
}
