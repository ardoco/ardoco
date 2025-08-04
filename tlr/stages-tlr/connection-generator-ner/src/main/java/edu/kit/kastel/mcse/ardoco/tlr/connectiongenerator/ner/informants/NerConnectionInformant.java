/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.tlr.connectiongenerator.ner.informants;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

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
    public static final double DEFAULT_PROBABILITY = 0.92;
    private static final String promptTemplate = """
            You get two named architecture entities with their names and alternative names. 
            Are these entities equivalent and are semantically similar? Answer only with "Yes" or "No".
            The first entity is "{first}" with the following alternative names: {first_alt}.
            The second entity is "{second}". 
            """;

    private SimilarityUtils similarityUtils;
    private NerConnectionStatesImpl nerConnectionStates; // Use Impl to access distinct fields/methods
    private ModelStates modelStatesData;

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
        }
    }

    private void processForMetamodel(Metamodel metamodel) {
        var modelEndpoints = modelStatesData.getModel(metamodel).getEndpoints();
        var nerConnectionState = nerConnectionStates.getNerConnectionState(metamodel);
        var namedArchitectureEntities = nerConnectionState.getNamedArchitectureEntities();
        var nonMatchedNamedArchitectureEntities = Lists.mutable.withAll(namedArchitectureEntities.toList());
        for (var namedArchitectureEntity : namedArchitectureEntities) {
            for (var modelEndpoint : modelEndpoints) {
                if (areNamedArchitectureEntityAndModelEndpointSimilar(namedArchitectureEntity, modelEndpoint)) {
                    createTraceLinks(nerConnectionState, namedArchitectureEntity, modelEndpoint);
                    nonMatchedNamedArchitectureEntities.remove(namedArchitectureEntity);
                }
            }
        }

        for (var namedArchitectureEntity : nonMatchedNamedArchitectureEntities) {
            for (var modelEndpoint : modelEndpoints) {
                if (areNamedArchitectureEntityAndModelEndpointWeaklySimilar(namedArchitectureEntity, modelEndpoint)) {
                    createTraceLinks(nerConnectionState, namedArchitectureEntity, modelEndpoint);
                    nonMatchedNamedArchitectureEntities.remove(namedArchitectureEntity);
                }
            }
        }

        System.out.println("Still not matched: " + nonMatchedNamedArchitectureEntities.size());
        var start = Instant.now();
        for (var namedArchitectureEntity : nonMatchedNamedArchitectureEntities) {
            for (var modelEndpoint : modelEndpoints) {
                if (areNamedArchitectureEntityAndModelEndpointSimilarLlm(namedArchitectureEntity, modelEndpoint)) {
                    createTraceLinks(nerConnectionState, namedArchitectureEntity, modelEndpoint);
                    nonMatchedNamedArchitectureEntities.remove(namedArchitectureEntity);
                }
            }
        }
        Instant end = Instant.now();
        System.out.println("Time: " + Duration.between(start, end).toMillis());

        System.out.println("At the end not matched: " + nonMatchedNamedArchitectureEntities.size());
        for (var namedArchitectureEntity : nonMatchedNamedArchitectureEntities) {
            System.out.println(namedArchitectureEntity);
        }

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

    private boolean areNamedArchitectureEntityAndModelEndpointSimilarLlm(NamedArchitectureEntity namedArchitectureEntity, ModelEntity modelEndpoint) {
        //        String prompt = promptTemplate.replace("{first}", namedArchitectureEntity.getName());
        //        prompt = prompt.replace("{first_alt}", StringUtils.join(namedArchitectureEntity.getAlternativeNames(), ", "));
        //        prompt = prompt.replace("{second}", modelEndpoint.getName());
        //        var llmResponse = this.chatModel.chat(prompt).toLowerCase();
        //        return llmResponse.contains("yes");
        var namedArchitectureEntityNames = Lists.mutable.withAll(namedArchitectureEntity.getAlternativeNames());
        namedArchitectureEntityNames.add(namedArchitectureEntity.getName());
        var namedArchitectureEntityEmbeddings = embed(namedArchitectureEntityNames);
        var modelEndpointNames = Lists.mutable.of(modelEndpoint.getName());
        var modelEndpointEmbeddings = embed(modelEndpointNames);

        for (var namedArchitectireEntityEmbedding : namedArchitectureEntityEmbeddings) {
            for (var modelEndpointEmbedding : modelEndpointEmbeddings) {
                var similarity = CosineSimilarity.between(namedArchitectireEntityEmbedding, modelEndpointEmbedding);
                System.out.println("Similarity of " + similarity + " for: " + namedArchitectureEntity.getName() + " <-> " + modelEndpoint.getName());
                if (similarity >= 0.88) {
                    System.out.println("Similarity of " + similarity + " for: " + namedArchitectureEntity.getName() + " <-> " + modelEndpoint.getName());
                    return true;
                }
            }
        }

        return false;
    }

    private List<Embedding> embed(List<String> names) {
        var embeddingModel = nerConnectionStates.getLlmSettings().createEmbeddingModel();
        var segments = names.stream().map(name -> TextSegment.from(name)).toList();
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
