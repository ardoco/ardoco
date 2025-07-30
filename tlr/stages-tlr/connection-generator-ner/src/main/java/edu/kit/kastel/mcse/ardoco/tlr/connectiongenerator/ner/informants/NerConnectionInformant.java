package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionState;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class NerConnectionInformant extends Informant {
    public static final double DEFAULT_PROBABILITY = 1.0;
    private SimilarityUtils similarityUtils;

    public NerConnectionInformant(DataRepository dataRepository) {
        super(NerConnectionInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void process() {
        // TODO
        similarityUtils = SimilarityUtils.getInstance();

        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        var nerConnectionStates = DataRepositoryHelper.getNerConnectionStates(dataRepository);
        for (var metamodel : modelStatesData.getMetamodels()) {
            var modelEndpoints = modelStatesData.getModel(metamodel).getEndpoints();
            var nerConnectionState = nerConnectionStates.getNerConnectionState(metamodel);
            var namedArchitectureEntities = nerConnectionState.getNamedArchitectureEntities();
            for (var namedArchitectureEntity : namedArchitectureEntities) {
                for (var modelEndpoint : modelEndpoints) {
                    if (areNamedArchitectureEntityAndModelEndpointSimilar(namedArchitectureEntity, modelEndpoint)) {
                        createTraceLinks(nerConnectionState, namedArchitectureEntity, modelEndpoint);
                    }
                }
            }
        }
    }

    private boolean areNamedArchitectureEntityAndModelEndpointSimilar(NamedArchitectureEntity namedArchitectureEntity, ModelEntity modelEndpoint) {
        var name = namedArchitectureEntity.getName();
        var modelEndpointName = modelEndpoint.getName();
        if (similarityUtils.areWordsSimilar(name, modelEndpointName)) {
            return true;
        }

        for (var alternativeName : namedArchitectureEntity.getAlternativeNames()) {
            if (similarityUtils.areWordsSimilar(alternativeName, modelEndpointName)) {
                return true;
            }
        }

        // TODO weaker similarity like only part of the names when separated at Camel Case
        return false;
    }

    private void createTraceLinks(NerConnectionState nerConnectionState, NamedArchitectureEntity namedArchitectureEntity, ModelEntity modelEntity) {
        for (var occurrence : namedArchitectureEntity.getOccurrences()) {
            nerConnectionState.addToLinks(occurrence, modelEntity, this, DEFAULT_PROBABILITY);
        }
    }
}
