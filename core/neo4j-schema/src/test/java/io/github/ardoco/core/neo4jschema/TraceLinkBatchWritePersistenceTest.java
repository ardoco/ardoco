/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.collections.api.factory.SortedMaps;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.RecommendationModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.tlr.recommendationgenerator.RecommendedInstanceImpl;
import io.github.ardoco.core.neo4jschema.service.MissingTraceLinkEndpointException;
import io.github.ardoco.core.neo4jschema.service.TraceLinkPersistenceService;
import io.github.ardoco.core.neo4jschema.util.models.ArchitectureModelFactory;
import io.github.ardoco.core.neo4jschema.util.models.CodeModelFactory;
import io.github.ardoco.core.neo4jschema.util.models.TextFactory;

@SpringBootTest(classes = Main.class)
class TraceLinkBatchWritePersistenceTest extends AbstractNeo4jTest {

    private static final Claimant TEST_CLAIMANT = new Claimant() {
    };

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    private ArchitectureItem architectureItem;
    private CodeItem codeItem;
    private CodeItem secondCodeItem;

    @BeforeEach
    void setUp() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
        ArchitectureModel architectureModel = ArchitectureModelFactory.createArchitectureModel();
        CodeModel codeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, architectureModel);
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);
        architectureItem = architectureModel.getEndpoints().get(0);
        codeItem = codeModel.getEndpoints().get(0);
        secondCodeItem = codeModel.getEndpoints().get(1);
    }

    @AfterEach
    void tearDown() {
        PersistenceBridge.setHandler(null);
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.empty());
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    @DisplayName("Trace links saved in several calls within one stage are written in one handler call, deduplicated by (source, target, type)")
    void deduplicatesAcrossTheStage() {
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));

        List<ILoggingEvent> events = captureTraceLinkServiceLog(() -> PersistenceBridge.withDeferredWrites("stage-dedup", () -> {
            PersistenceBridge.saveTraceLinks(List.of(new ArchitectureCodeTraceLink(architectureItem, codeItem)));
            PersistenceBridge.saveTraceLinks(List.of(new ArchitectureCodeTraceLink(architectureItem, codeItem), new ArchitectureCodeTraceLink(architectureItem,
                    secondCodeItem)));
            return null;
        }));

        Assertions.assertEquals(1, debugSummaries(events).size(), "One stage flush must hand all trace links to the handler in one call");
        Assertions.assertTrue(debugSummaries(events).getFirst().contains("3 rows before dedup, 2 after dedup"), debugSummaries(events).getFirst());
        Assertions.assertEquals(2L, countTraceLinks("ARCHITECTURE_CODE"));
    }

    @Test
    @DisplayName("Both legs of every transitive link are kept; shared legs are deduplicated, never dropped")
    void keepsTransitiveLegs() {
        Text text = TextFactory.createComplexText("batch_transitive_doc");
        persistenceHandler.savePreprocessedText(text, "PreprocessingData");
        Assertions.assertTrue(text.getSentences().size() >= 2, "the test text needs at least two sentences");
        Sentence first = text.getSentences().get(0);
        Sentence second = text.getSentences().get(1);
        ArchitectureCodeTraceLink sharedLeg = new ArchitectureCodeTraceLink(architectureItem, codeItem);
        List<TraceLink<?, ?>> links = List.of(transitive(first, sharedLeg), transitive(second, sharedLeg), sharedLeg);

        List<ILoggingEvent> events = captureTraceLinkServiceLog(() -> persistenceHandler.saveTraceLinks(links));

        Assertions.assertTrue(debugSummaries(events).getFirst().contains("5 rows before dedup, 3 after dedup"), debugSummaries(events).getFirst());
        Assertions.assertEquals(2L, countTraceLinks("SENTENCE_ARCHITECTURE"));
        Assertions.assertEquals(1L, countTraceLinks("ARCHITECTURE_CODE"));
    }

    @Test
    @DisplayName("Recommendation links (source :RecommendedInstance) and other links (source :Traceable) are written by their own query shape")
    void writesBothQueryShapes() {
        String recommendedInstanceId = "ri-batch-shape-test";
        neo4jClient.query("CREATE (:RecommendedInstance {ardocoId: $id})").bind(recommendedInstanceId).to("id").run();
        var recommendedInstance = new RecommendedInstanceImpl("batch", "component", recommendedInstanceId);
        var recommendationLink = new RecommendationModelTraceLink(recommendedInstance, architectureItem, TEST_CLAIMANT, 0.42);
        List<TraceLink<?, ?>> links = List.of(recommendationLink, new ArchitectureCodeTraceLink(architectureItem, codeItem));

        List<ILoggingEvent> events = captureTraceLinkServiceLog(() -> persistenceHandler.saveTraceLinks(links));

        Assertions.assertTrue(debugSummaries(events).getFirst().contains("2 UNWIND queries"), debugSummaries(events).getFirst());
        Assertions.assertEquals(1L, countTraceLinks("RECOMMENDATION_ARCHITECTURE"));
        Assertions.assertEquals(1L, countTraceLinks("ARCHITECTURE_CODE"));
        Double storedConfidence = neo4jClient.query("""
                MATCH (:RecommendedInstance {ardocoId: $id})-[r:TRACES_TO]->() RETURN r.confidence AS confidence
                """)
                .bind(recommendedInstanceId)
                .to("id")
                .fetchAs(Double.class)
                .mappedBy((typeSystem, neo4jRecord) -> neo4jRecord.get("confidence").asDouble())
                .one()
                .orElseThrow();
        Assertions.assertEquals(recommendationLink.getConfidence(), storedConfidence, 1e-9);
    }

    @Test
    @DisplayName("In a mixed batch, rows with both endpoints are written and rows with a missing endpoint fail the call with one ERROR")
    void reportsMissingEndpointsInAMixedBatch() {
        Text text = TextFactory.createComplexText("batch_missing_doc");
        persistenceHandler.savePreprocessedText(text, "PreprocessingData");
        List<TraceLink<?, ?>> links = List.of(new ArchitectureCodeTraceLink(architectureItem, codeItem), new ArchitectureCodeTraceLink(new ArchitectureMethod(
                "ghostSource", "ghost-source-id"), codeItem), new SentenceModelTraceLink(text.getSentences().get(0), new ArchitectureMethod("ghostTarget",
                        "ghost-target-id")));

        List<MissingTraceLinkEndpointException> thrown = new ArrayList<>();
        List<ILoggingEvent> events = captureTraceLinkServiceLog(() -> thrown.add(Assertions.assertThrows(MissingTraceLinkEndpointException.class,
                () -> persistenceHandler.saveTraceLinks(links))));

        Assertions.assertEquals(1L, countTraceLinks("ARCHITECTURE_CODE"));
        Assertions.assertEquals(0L, countTraceLinks("SENTENCE_ARCHITECTURE"));
        List<String> errors = errors(events);
        Assertions.assertEquals(1, errors.size(), errors.toString());
        String error = errors.getFirst();
        Assertions.assertTrue(error.contains("2 trace link(s) not written"), error);
        Assertions.assertTrue(error.contains("ARCHITECTURE_CODE: 1 (source 1, target 0)"), error);
        Assertions.assertTrue(error.contains("SENTENCE_ARCHITECTURE: 1 (source 0, target 1)"), error);
        Assertions.assertTrue(error.contains("ghost-source-id"), error);
        Assertions.assertTrue(error.contains("ghost-target-id"), error);
        Assertions.assertEquals(error, thrown.getFirst().getMessage());
        Assertions.assertTrue(events.stream().noneMatch(event -> event.getLevel() == Level.WARN), events.toString());
        Assertions.assertTrue(debugSummaries(events).getFirst().contains("2 rows not written"), debugSummaries(events).getFirst());
    }

    @Test
    @DisplayName("More than 2000 rows are written in several UNWIND chunks, all of them")
    void writesRowsInChunks() {
        int linkCount = 2101;
        List<String> ids = new ArrayList<>();
        List<TraceLink<?, ?>> links = new ArrayList<>();
        for (int i = 0; i < linkCount; i++) {
            String id = "batch-chunk-method-" + i;
            ids.add(id);
            links.add(new ArchitectureCodeTraceLink(new ArchitectureMethod("method" + i, id), codeItem));
        }
        neo4jClient.query("UNWIND $ids AS id CREATE (:ArchitectureItem:ArchitectureMethod:Traceable {ardocoId: id})").bind(ids).to("ids").run();

        List<ILoggingEvent> events = captureTraceLinkServiceLog(() -> persistenceHandler.saveTraceLinks(links));

        Assertions.assertEquals(linkCount, countTraceLinks("ARCHITECTURE_CODE"));
        Assertions.assertTrue(debugSummaries(events).getFirst().contains("2 UNWIND queries"), debugSummaries(events).getFirst());
        Assertions.assertTrue(debugSummaries(events).getFirst().contains("0 rows not written"), debugSummaries(events).getFirst());
    }

    private static TransitiveTraceLink<?, ?> transitive(Sentence sentence, ArchitectureCodeTraceLink architectureCodeLink) {
        return TransitiveTraceLink.createTransitiveTraceLink(new SentenceModelTraceLink(sentence, architectureCodeLink.getFirstEndpoint()),
                architectureCodeLink).orElseThrow();
    }

    private long countTraceLinks(String type) {
        return neo4jClient.query("MATCH ()-[r:TRACES_TO]->() WHERE r.traceLinkType = $type RETURN count(r) AS c")
                .bind(type)
                .to("type")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, neo4jRecord) -> neo4jRecord.get("c").asLong())
                .one()
                .orElse(0L);
    }

    private static List<ILoggingEvent> captureTraceLinkServiceLog(Runnable action) {
        Logger logger = (Logger) LoggerFactory.getLogger(TraceLinkPersistenceService.class);
        Level previousLevel = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.DEBUG);
        try {
            action.run();
        } finally {
            logger.setLevel(previousLevel);
            logger.detachAppender(appender);
        }
        return appender.list;
    }

    private static List<String> debugSummaries(List<ILoggingEvent> events) {
        return events.stream()
                .filter(event -> event.getLevel() == Level.DEBUG)
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.startsWith("saveTracelinks:"))
                .toList();
    }

    private static List<String> errors(List<ILoggingEvent> events) {
        return events.stream().filter(event -> event.getLevel() == Level.ERROR).map(ILoggingEvent::getFormattedMessage).toList();
    }
}
