package edu.kit.kastel.mcse.ardoco.tlr.artemis.informants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.artemis.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public class ComponentArtemisConnectionInformant extends ArtemisConnectionInformant {

    public ComponentArtemisConnectionInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(dataRepository, strategy);
    }

    @Override
    protected void process() {
        var similarityUtils = SimilarityUtils.getInstance();

        var state = getConnectionState();
        var modelEndpoints = getModelStatesData().getModel(strategy.getMetamodel()).getEndpoints();

        var namedEntities = state.getNamedEntities();
        var unlinkedNamedEntities = Lists.mutable.withAll(namedEntities);
        var matchedNamedEntities = Lists.mutable.empty();
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

        state.addTraceLinks(traceLinks);
        state.addUnlinkedNamedEntities(unlinkedNamedEntities);
    }
}
