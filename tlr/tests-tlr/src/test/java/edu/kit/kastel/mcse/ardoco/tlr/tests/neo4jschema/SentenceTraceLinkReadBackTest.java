/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Transarc;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.tests.neo4jschema.util.PersistenceTestSupport;

/**
 * Regression test for sentence trace links ({@code ConnectionState#getTraceLinks()}) and their persisted graph shape.
 * <p>
 * (1) During a run, the in-memory state is authoritative: the sentence trace links of every connection state must equal the DataRepository-only run,
 * per metamodel (previously the architecture state also returned the sentence links of the code metamodel, TeaStore: 120 instead of 20).
 * <p>
 * (2) Graph check: nothing reads Neo4j during a run (memory is authoritative, {@code ConnectionState#getTraceLinks()} has no database fallback), so
 * metamodel correctness of the persisted graph is checked directly via {@code TRACES_TO} counts per type instead of a read-back API: the number of
 * {@code SENTENCE_ARCHITECTURE}/{@code SENTENCE_CODE} trace links written for each metamodel must equal the in-memory count of the same run.
 */
class SentenceTraceLinkReadBackTest extends AbstractPersistenceTest {

    @Test
    @DisplayName("TransArC with base persistence: sentence trace links per metamodel equal the DataRepository-only run; the written graph matches by type")
    void testBasePersistenceSentenceTraceLinks() {
        assertSentenceTraceLinkParityAndGraphCounts(false, false);
    }

    @Test
    @DisplayName("TransArC with TextState/Recommendation dual-write: sentence trace links per metamodel equal the DataRepository-only run; the written graph matches by type")
    void testDualWriteSentenceTraceLinks() {
        assertSentenceTraceLinkParityAndGraphCounts(true, true);
    }

    private void assertSentenceTraceLinkParityAndGraphCounts(boolean persistTextState, boolean persistRecommendations) {
        clearNeo4jGraph();
        Map<Metamodel, SortedSet<String>> baseline = runTransarcAndCollectSentenceLinks(false, false, false);
        clearNeo4jGraph();
        Map<Metamodel, SortedSet<String>> withPersistence = runTransarcAndCollectSentenceLinks(true, persistTextState, persistRecommendations);

        // (1) Normal run: memory is authoritative, links are identical per metamodel.
        Assertions.assertEquals(baseline.keySet(), withPersistence.keySet(), "Active metamodels must match");
        for (Metamodel metamodel : baseline.keySet()) {
            Assertions.assertEquals(baseline.get(metamodel), withPersistence.get(metamodel),
                    "Sentence trace links of " + metamodel + " must equal the DataRepository-only run");
        }

        // (2) Graph check: the persisted TRACES_TO counts per type must match the in-memory counts of the same run.
        Assertions.assertTrue(PersistenceBridge.isAvailable(), "Persistence must be available after the persistence-enabled run");
        for (Metamodel metamodel : baseline.keySet()) {
            String traceLinkType = metamodel.isArchitectureModel() ? "SENTENCE_ARCHITECTURE" : "SENTENCE_CODE";
            Assertions.assertEquals(baseline.get(metamodel).size(), countTraceLinksByType(traceLinkType),
                    "Persisted " + traceLinkType + " count must equal the in-memory sentence trace links of " + metamodel);
        }

        pauseForNeo4jInspection();
    }

    private long countTraceLinksByType(String traceLinkType) {
        return neo4jClient.query("""
                MATCH (:Sentence)-[r:TRACES_TO]->(:Traceable)
                WHERE r.traceLinkType = $type
                RETURN count(r) AS c
                """)
                .bind(traceLinkType)
                .to("type")
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }

    private Map<Metamodel, SortedSet<String>> runTransarcAndCollectSentenceLinks(boolean usePersistence, boolean persistTextState,
            boolean persistRecommendations) {
        var runner = new Transarc(projectName);
        runner.setUp(new File(inputText), new ArchitectureConfiguration(new File(inputModelArchitecture), ModelFormat.PCM), codeConfiguration,
                getConfigsWithPersistence(usePersistence, persistTextState, persistRecommendations), new File(outputDir));
        testRunnerAssertions(runner);
        var result = runner.run();
        Assertions.assertNotNull(result);

        var dataRepository = runner.getArdoco().getDataRepository();
        var connectionStates = dataRepository.getData(ConnectionStates.ID, ConnectionStates.class).orElseThrow();
        Map<Metamodel, SortedSet<String>> keysByMetamodel = new EnumMap<>(Metamodel.class);
        for (Metamodel metamodel : PersistenceTestSupport.activeMetamodels(dataRepository)) {
            var connectionState = connectionStates.getConnectionState(metamodel);
            if (connectionState == null) {
                continue;
            }
            var traceLinks = connectionState.getTraceLinks();
            for (TraceLink<SentenceEntity, ModelEntity> link : traceLinks) {
                assertTargetBelongsTo(metamodel, link.getSecondEndpoint());
            }
            keysByMetamodel.put(metamodel, keys(traceLinks));
        }
        return keysByMetamodel;
    }

    private static void assertTargetBelongsTo(Metamodel metamodel, ModelEntity target) {
        if (metamodel.isArchitectureModel()) {
            Assertions.assertInstanceOf(ArchitectureEntity.class, target, "Architecture state contains a non-architecture target: " + target.getId());
        } else if (metamodel.isCodeModel()) {
            Assertions.assertInstanceOf(CodeItem.class, target, "Code state contains a non-code target: " + target.getId());
        }
    }

    private static SortedSet<String> keys(Iterable<TraceLink<SentenceEntity, ModelEntity>> links) {
        SortedSet<String> keys = new TreeSet<>();
        for (TraceLink<SentenceEntity, ModelEntity> link : links) {
            keys.add(link.getFirstEndpoint().getSentence().getSentenceNumber() + "|" + link.getSecondEndpoint().getId());
        }
        return keys;
    }
}
