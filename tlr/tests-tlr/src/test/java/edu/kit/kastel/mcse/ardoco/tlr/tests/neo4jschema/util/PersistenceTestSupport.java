/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArdocoResult;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendationStates;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;

/**
 * Shared assertions for Neo4j persistence tests (active metamodels only — not {@link Metamodel#values()}).
 */
public final class PersistenceTestSupport {

    private PersistenceTestSupport() {
    }

    public static Collection<Metamodel> activeMetamodels(DataRepository dataRepository) {
        return dataRepository.getData(ModelStates.ID, ModelStates.class).orElseThrow().getMetamodels();
    }

    public static long countRecommendedInstances(RecommendationStates recommendationStates, Collection<Metamodel> activeMetamodels) {
        long total = 0;
        for (Metamodel metamodel : activeMetamodels) {
            var state = recommendationStates.getRecommendationState(metamodel);
            if (state != null) {
                total += state.getRecommendedInstances().size();
            }
        }
        return total;
    }

    public static SortedSet<String> nounMappingSemanticKeys(TextState textState) {
        SortedSet<String> keys = new TreeSet<>();
        for (NounMapping mapping : textState.getNounMappings()) {
            keys.add(mapping.getReference() + "|" + mapping.getKind());
        }
        return keys;
    }

    /**
     * Per-RI fingerprint of linked NounMapping ids (name + type mappings) for resume parity.
     */
    public static SortedSet<String> recommendedInstanceNounMappingKeys(RecommendationStates recommendationStates, Collection<Metamodel> activeMetamodels) {
        SortedSet<String> keys = new TreeSet<>();
        for (Metamodel metamodel : activeMetamodels) {
            var state = recommendationStates.getRecommendationState(metamodel);
            if (state == null) {
                continue;
            }
            for (RecommendedInstance ri : state.getRecommendedInstances()) {
                SortedSet<String> mappingIds = new TreeSet<>();
                ri.getNameMappings().forEach(nm -> mappingIds.add("n:" + nm.getArdocoId()));
                ri.getTypeMappings().forEach(nm -> mappingIds.add("t:" + nm.getArdocoId()));
                String mappings = mappingIds.stream().collect(Collectors.joining(","));
                keys.add(ri.getId() + "|" + mappings);
            }
        }
        return keys;
    }

    public static SortedSet<String> recommendedInstanceSemanticKeys(RecommendationStates recommendationStates, Collection<Metamodel> activeMetamodels) {
        SortedSet<String> keys = new TreeSet<>();
        for (Metamodel metamodel : activeMetamodels) {
            var state = recommendationStates.getRecommendationState(metamodel);
            if (state == null) {
                continue;
            }
            for (RecommendedInstance ri : state.getRecommendedInstances()) {
                keys.add(ri.getName().toLowerCase() + "|" + ri.getType().toLowerCase());
            }
        }
        return keys;
    }

    public static SortedSet<String> architectureTraceLinkSemanticKeys(ArdocoResult result) {
        SortedSet<String> keys = new TreeSet<>();
        for (TraceLink<SentenceEntity, ModelEntity> link : result.getArchitectureTraceLinks()) {
            keys.add(link.getFirstEndpoint().getSentence().getSentenceNumber() + "|" + link.getSecondEndpoint().getId());
        }
        return keys;
    }
}
