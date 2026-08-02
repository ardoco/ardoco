package edu.kit.kastel.mcse.ardoco.tlr.artemis.informants;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.CosineSimilarity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.Environment;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisTraceabilityState;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisTraceabilityStates;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.states.ArtemisTraceabilityStatesImpl;
import edu.kit.kastel.mcse.ardoco.tlr.artemis.strategies.ArtemisNerStrategy;

public abstract class ArtemisConnectionInformant extends Informant {
    public static final double DEFAULT_PROBABILITY = 0.92;
    public static final double EMBEDDING_SIMILARITY_THRESHOLD = 0.6;

    protected static final Logger logger = LoggerFactory.getLogger(ArtemisConnectionInformant.class);
    protected final ArtemisNerStrategy strategy;
    private final Map<ModelEntity, List<Embedding>> modelEntityEmbeddings = new LinkedHashMap<>();
    private final Map<NamedArchitectureEntity, List<Embedding>> namedArchitectureEntityEmbeddings = new LinkedHashMap<>();

    protected ArtemisConnectionInformant(DataRepository dataRepository, ArtemisNerStrategy strategy) {
        super(strategy.getId() + "ConnectionInformant", dataRepository);
        this.strategy = strategy;
    }

    protected ArtemisTraceabilityState getTraceabilityState() {
        ArtemisTraceabilityStates states = dataRepository.getData(ArtemisTraceabilityStates.ID, ArtemisTraceabilityStatesImpl.class).orElseThrow();
        return states.getState(strategy.getTarget());
    }

    protected ModelStates getModelStatesData() {
        return DataRepositoryHelper.getModelStatesData(dataRepository);
    }

    protected boolean areStronglySimilar(SimilarityUtils similarityUtils, NamedArchitectureEntity namedEntity, ModelEntity modelEndpoint) {
        var entityName = namedEntity.getName();
        var modelEndpointName = Objects.requireNonNull(modelEndpoint.getName());

        if (similarityUtils.areWordsSimilar(entityName, modelEndpointName) || similarityUtils.areWordsSimilar(modelEndpointName, entityName)) {
            return true;
        }

        for (var alternativeName : namedEntity.getAlternativeNames()) {
            if (similarityUtils.areWordsSimilar(alternativeName, modelEndpointName) || similarityUtils.areWordsSimilar(modelEndpointName, alternativeName)) {
                return true;
            }
        }

        return false;
    }

    protected boolean areWeaklySimilar(SimilarityUtils similarityUtils, NamedArchitectureEntity namedEntity, ModelEntity modelEndpoint) {
        var entityNameParts = namedEntity.getNameParts();
        var modelEndpointNameParts = modelEndpoint.getNameParts();

        return similarityUtils.areWordsOfListsSimilar(entityNameParts, modelEndpointNameParts) || similarityUtils.areWordsOfListsSimilar(modelEndpointNameParts,
                entityNameParts);
    }

    protected boolean areWeaklySimilarIncludingAlternativeNameParts(SimilarityUtils similarityUtils, NamedArchitectureEntity namedEntity,
            ModelEntity modelEndpoint) {
        if (areWeaklySimilar(similarityUtils, namedEntity, modelEndpoint)) {
            return true;
        }

        var allEntityNameParts = namedEntity.getAllNameParts();
        var modelEndpointNameParts = modelEndpoint.getNameParts();

        return similarityUtils.areWordsOfListsSimilar(allEntityNameParts, modelEndpointNameParts) || similarityUtils.areWordsOfListsSimilar(
                modelEndpointNameParts, allEntityNameParts);
    }

    protected List<Embedding> getModelEndpointEmbeddings(ModelEntity modelEndpoint) {
        return this.modelEntityEmbeddings.computeIfAbsent(modelEndpoint, modelEntity -> {
            var modelEndpointNames = Lists.mutable.of(modelEntity.getName());
            return embed(modelEndpointNames);
        });
    }

    protected List<Embedding> getNamedArchitectureEntityEmbeddings(NamedArchitectureEntity namedArchitectureEntity) {
        return this.namedArchitectureEntityEmbeddings.computeIfAbsent(namedArchitectureEntity, nae -> {
            var namedArchitectureEntityNames = Lists.mutable.withAll(nae.getAlternativeNames());
            namedArchitectureEntityNames.add(nae.getName());
            return embed(namedArchitectureEntityNames);
        });
    }

    protected boolean embeddingsAreSimilar(List<Embedding> namedArchitectureEntityEmbeddings, List<Embedding> modelEndpointEmbeddings) {
        for (var namedArchitectureEntityEmbedding : namedArchitectureEntityEmbeddings) {
            for (var modelEndpointEmbedding : modelEndpointEmbeddings) {
                var similarity = CosineSimilarity.between(namedArchitectureEntityEmbedding, modelEndpointEmbedding);
                if (similarity >= EMBEDDING_SIMILARITY_THRESHOLD) {
                    logger.debug("Similarity of {}", similarity);
                    return true;
                }
            }
        }
        return false;
    }

    protected List<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> createTraceLinks(NamedArchitectureEntity namedEntity, ModelEntity modelEntity) {
        List<TraceLink<NamedArchitectureEntityOccurrence, ModelEntity>> traceLinks = new ArrayList<>();

        /*var lines = this.getDataRepository().getData("SimplePreprocessingData", SimplePreprocessingData.class).get().getText().getLines(); //TODO tmp test - add direct occurrences
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            var lineNumber = i+1;
            //remove whitespace and all non-alphanumeric characters
            var searchiLine = line.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (searchiLine.contains(namedEntity.getName().toLowerCase())){
                namedEntity.getOccurrences().add(new NamedArchitectureEntityOccurrence(namedEntity.getName(),lineNumber)); // use cooler method... + logger.debug info
            }
        }*/

        for (var occurrence : namedEntity.getOccurrences()) {
            traceLinks.add(new NamedArchitectureEntityToModelTraceLink(occurrence, modelEntity, this, DEFAULT_PROBABILITY));
        }

        return traceLinks;
    }

    private List<Embedding> embed(List<String> names) {
        var openaiApiKey = Environment.getEnv("OPENAI_API_KEY");
        if (openaiApiKey == null) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable is not set. Please set it to use the OpenAI embedding model.");
        }
        var embeddingModel = new OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder().modelName("text-embedding-3-large").apiKey(openaiApiKey).build();
        var segments = names.stream().map(TextSegment::from).toList();
        return embeddingModel.embedAll(segments).content();
    }
}
