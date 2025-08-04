/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants;

import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.CosineSimilarity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NerConnectionState;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.NerConnectionStatesImpl;

public class NerConnectionInformant extends Informant {
    private static final Logger logger = LoggerFactory.getLogger(NerConnectionInformant.class);

    public static final double DEFAULT_PROBABILITY = 0.92;
    public static final double EMBEDDING_SIMILARITY_THRESHOLD = 0.6;

    private SimilarityUtils similarityUtils;
    private NerConnectionStatesImpl nerConnectionStates; // Use Impl to access distinct fields/methods
    private ModelStates modelStatesData;
    private final Map<ModelEntity, List<Embedding>> modelEntityEmbeddings = Maps.mutable.empty();
    private final Map<NamedArchitectureEntity, List<Embedding>> namedArchitectureEntityEmbeddings = Maps.mutable.empty();

    public NerConnectionInformant(DataRepository dataRepository) {
        super(NerConnectionInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void process() {
        similarityUtils = SimilarityUtils.getInstance();

        this.modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        nerConnectionStates = DataRepositoryHelper.getNerConnectionStates(dataRepository).asPipelineStepData(NerConnectionStatesImpl.class).orElseThrow();
        for (var metamodel : modelStatesData.getMetamodels()) {
            processForMetamodel(metamodel);
            if (logger.isDebugEnabled()) {
                int numberOfUnmatchedEntities = nerConnectionStates.getNerConnectionState(metamodel).getUnlinkedNamedArchitectureEntities().size();
                logger.debug("For metamodel {} at the end not matched: {} NAEs", metamodel, numberOfUnmatchedEntities);
            }
        }
    }

    private void processForMetamodel(Metamodel metamodel) {
        var modelEndpoints = modelStatesData.getModel(metamodel).getEndpoints();
        var nerConnectionState = nerConnectionStates.getNerConnectionState(metamodel);
        var namedArchitectureEntities = nerConnectionState.getNamedArchitectureEntities();

        // Try to match with similarity metrics
        var nonMatchedNamedArchitectureEntities = Lists.mutable.withAll(namedArchitectureEntities.toList());
        var matchedNamedArchitectureEntities = Lists.mutable.empty();
        for (var namedArchitectureEntity : namedArchitectureEntities) {
            for (var modelEndpoint : modelEndpoints) {
                if (areNamedArchitectureEntityAndModelEndpointSimilar(namedArchitectureEntity, modelEndpoint)) {
                    createTraceLinks(nerConnectionState, namedArchitectureEntity, modelEndpoint);
                    matchedNamedArchitectureEntities.add(namedArchitectureEntity);
                }
            }
        }
        nonMatchedNamedArchitectureEntities.removeAll(matchedNamedArchitectureEntities);
        matchedNamedArchitectureEntities = Lists.mutable.empty();

        // try to match with weaker similarity comparison
        for (var namedArchitectureEntity : nonMatchedNamedArchitectureEntities) {
            for (var modelEndpoint : modelEndpoints) {
                if (areNamedArchitectureEntityAndModelEndpointWeaklySimilar(namedArchitectureEntity, modelEndpoint)) {
                    createTraceLinks(nerConnectionState, namedArchitectureEntity, modelEndpoint);
                    matchedNamedArchitectureEntities.add(namedArchitectureEntity);
                }
            }
        }
        nonMatchedNamedArchitectureEntities.removeAll(matchedNamedArchitectureEntities);
        matchedNamedArchitectureEntities = Lists.mutable.empty();

        // try to match using embedding similarity
        for (var namedArchitectureEntity : nonMatchedNamedArchitectureEntities) {
            logger.debug("Trying to match using embeddings for the NAE: {}", namedArchitectureEntity.getName());
            var currentNamedArchitectureEntityEmbeddings = getNamedArchitectureEntityEmbeddings(namedArchitectureEntity);
            for (var modelEndpoint : modelEndpoints) {
                var modelEndpointEmbeddings = getModelEndpointEmbeddings(modelEndpoint);
                if (embeddingsAreSimilar(currentNamedArchitectureEntityEmbeddings, modelEndpointEmbeddings)) {
                    logger.debug("^ similarity for {} <-> {}", namedArchitectureEntity.getName(), modelEndpoint.getName());
                    createTraceLinks(nerConnectionState, namedArchitectureEntity, modelEndpoint);
                    matchedNamedArchitectureEntities.add(namedArchitectureEntity);
                }
            }
        }
        nonMatchedNamedArchitectureEntities.removeAll(matchedNamedArchitectureEntities);

        nerConnectionState.addUnlinkedNamedArchitectureEntities(nonMatchedNamedArchitectureEntities);
    }

    private boolean embeddingsAreSimilar(List<Embedding> namedArchitectureEntityEmbeddings, List<Embedding> modelEndpointEmbeddings) {
        for (var namedArchitectireEntityEmbedding : namedArchitectureEntityEmbeddings) {
            for (var modelEndpointEmbedding : modelEndpointEmbeddings) {
                var similarity = CosineSimilarity.between(namedArchitectireEntityEmbedding, modelEndpointEmbedding);
                if (similarity >= EMBEDDING_SIMILARITY_THRESHOLD) {
                    logger.debug("Similarity of {}", similarity);
                    return true;
                }
            }
        }
        return false;
    }

    private List<Embedding> getModelEndpointEmbeddings(ModelEntity modelEndpoint) {
        return this.modelEntityEmbeddings.computeIfAbsent(modelEndpoint, modelEntity -> {
            var modelEndpointNames = Lists.mutable.of(modelEntity.getName());
            return embed(modelEndpointNames);
        });
    }

    private List<Embedding> getNamedArchitectureEntityEmbeddings(NamedArchitectureEntity namedArchitectureEntity) {
        return this.namedArchitectureEntityEmbeddings.computeIfAbsent(namedArchitectureEntity, nae -> {
            var namedArchitectureEntityNames = Lists.mutable.withAll(nae.getAlternativeNames());
            namedArchitectureEntityNames.add(nae.getName());
            return embed(namedArchitectureEntityNames);
        });
    }

    private boolean areNamedArchitectureEntityAndModelEndpointSimilar(NamedArchitectureEntity namedArchitectureEntity, ModelEntity modelEndpoint) {
        var name = namedArchitectureEntity.getName();
        var modelEndpointName = modelEndpoint.getName();
        if (similarityUtils.areWordsSimilar(name, modelEndpointName) || similarityUtils.areWordsSimilar(modelEndpointName, name)) {
            return true;
        }

        for (var alternativeName : namedArchitectureEntity.getAlternativeNames()) {
            if (similarityUtils.areWordsSimilar(alternativeName, modelEndpointName) || similarityUtils.areWordsSimilar(modelEndpointName, alternativeName)) {
                return true;
            }
        }
        return false;
    }

    private boolean areNamedArchitectureEntityAndModelEndpointWeaklySimilar(NamedArchitectureEntity namedArchitectureEntity, ModelEntity modelEndpoint) {
        var name = namedArchitectureEntity.getName();
        var namedArchitectureEntityNameParts = getNameParts(name);
        var modelEndpointNameParts = modelEndpoint.getNameParts();

        return similarityUtils.areWordsOfListsSimilar(namedArchitectureEntityNameParts, modelEndpointNameParts) //
                || similarityUtils.areWordsOfListsSimilar(modelEndpointNameParts, namedArchitectureEntityNameParts);
    }

    private List<Embedding> embed(List<String> names) {
        var embeddingModel = nerConnectionStates.getLlmSettings().createEmbeddingModel();
        var segments = names.stream().map(TextSegment::from).toList();
        return embeddingModel.embedAll(segments).content();
    }

    private ImmutableList<String> getNameParts(String name) {
        var parts = Lists.mutable.with(name.split("\\s"));
        var camelCaseParts = Lists.mutable.with(name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"));
        parts.addAll(camelCaseParts);
        return parts.toImmutable();
    }

    private void createTraceLinks(NerConnectionState nerConnectionState, NamedArchitectureEntity namedArchitectureEntity, ModelEntity modelEntity) {
        for (var occurrence : namedArchitectureEntity.getOccurrences()) {
            nerConnectionState.addToLinks(occurrence, modelEntity, this, DEFAULT_PROBABILITY);
        }
    }
}
