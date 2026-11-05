/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.List;
import java.util.Map;

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
import edu.kit.kastel.mcse.ardoco.core.api.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.id.types.ModelEntityAbsentFromTextInconsistency;
import edu.kit.kastel.mcse.ardoco.id.types.TextEntityAbsentFromModelInconsistency;
import io.github.ardoco.core.neo4jschema.service.MissingLinkTargetException;
import io.github.ardoco.core.neo4jschema.util.FakeArchitectureEntity;
import io.github.ardoco.core.neo4jschema.util.models.ArchitectureModelFactory;
import io.github.ardoco.core.neo4jschema.util.models.TextFactory;

@SpringBootTest(classes = Main.class)
class InconsistencyWritePathTest extends AbstractNeo4jTest {

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    private Text text;

    @BeforeEach
    void setUp() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
        text = TextFactory.createComplexText("inconsistency_write_path");
        persistenceHandler.savePreprocessedText(text, PreprocessingData.ID);
    }

    @AfterEach
    void tearDown() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    @DisplayName("A 1-based text inconsistency on the last sentence is attached to that sentence (0-based n-1) and keeps its 1-based number")
    void lastSentenceInconsistencyAttachesToItsSentence() {
        int lastSentenceOneBased = text.getSentences().size();

        persistenceHandler.addInconsistencies(List.of(new TextEntityAbsentFromModelInconsistency("database", lastSentenceOneBased, 0.9, null)));

        List<Map<String, Object>> edges = neo4jClient.query("""
                MATCH (:Text {ardocoId: $textId})-[:HAS_SENTENCE]->(s:Sentence)-[:HAS_INCONSISTENCY]->(i:TextInconsistency)
                RETURN s.sentenceNumber AS sentence, i.sentenceNumber AS inconsistency
                """).bind(PreprocessingData.ID).to("textId").fetch().all().stream().toList();
        Assertions.assertEquals(1, edges.size(), edges.toString());
        Assertions.assertEquals(lastSentenceOneBased - 1, ((Number) edges.getFirst().get("sentence")).intValue());
        Assertions.assertEquals(lastSentenceOneBased, ((Number) edges.getFirst().get("inconsistency")).intValue());
    }

    @Test
    @DisplayName("Saving queued inconsistencies loads no entity and needs at most one query per inconsistency kind")
    void savingInconsistenciesLoadsNoEntities() {
        ArchitectureModel architectureModel = ArchitectureModelFactory.createArchitectureModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, architectureModel);
        List<Inconsistency> inconsistencies = List.of(new TextEntityAbsentFromModelInconsistency("login", 1, 0.9, null),
                new TextEntityAbsentFromModelInconsistency("component", 1, 0.8, null), new TextEntityAbsentFromModelInconsistency("database", 2, 0.7,
                        null), new ModelEntityAbsentFromTextInconsistency(architectureModel.getEndpoints().get(0)), new ModelEntityAbsentFromTextInconsistency(
                                architectureModel.getEndpoints().get(1)));

        List<String> statements = captureCypherStatements(() -> persistenceHandler.addInconsistencies(inconsistencies));

        Assertions.assertFalse(statements.isEmpty(), "the Cypher statement log must be captured");
        Assertions.assertTrue(statements.stream().noneMatch(statement -> statement.contains("RETURN n{")), statements.toString());
        Assertions.assertTrue(statements.size() <= 2, statements.size() + " statements: " + statements);
        Assertions.assertEquals(5L, neo4jClient.query("MATCH ()-[r:HAS_INCONSISTENCY]->() RETURN count(r) AS c")
                .fetchAs(Long.class)
                .one()
                .orElse(0L));
    }

    @Test
    @DisplayName("A model inconsistency without its model element fails with one ERROR naming the kind and the id")
    void modelInconsistencyWithoutParentFails() {
        var ghost = new ModelEntityAbsentFromTextInconsistency(new FakeArchitectureEntity("Ghost", "ghost-model-id", "Component"));

        List<ILoggingEvent> events = captureErrors(() -> Assertions.assertThrows(MissingLinkTargetException.class, () -> persistenceHandler.addInconsistencies(
                List.of(ghost))));

        List<String> errors = errorMessages(events);
        Assertions.assertEquals(1, errors.size(), errors.toString());
        Assertions.assertTrue(errors.getFirst().contains("Inconsistency"), errors.getFirst());
        Assertions.assertTrue(errors.getFirst().contains("ghost-model-id"), errors.getFirst());
    }

    @Test
    @DisplayName("A text inconsistency whose sentence does not exist fails with one ERROR naming the kind and the sentence")
    void textInconsistencyWithoutSentenceFails() {
        var ghost = new TextEntityAbsentFromModelInconsistency("ghost", 99, 0.5, null);

        List<ILoggingEvent> events = captureErrors(() -> Assertions.assertThrows(MissingLinkTargetException.class, () -> persistenceHandler.addInconsistencies(
                List.of(ghost))));

        List<String> errors = errorMessages(events);
        Assertions.assertEquals(1, errors.size(), errors.toString());
        Assertions.assertTrue(errors.getFirst().contains("Inconsistency"), errors.getFirst());
        Assertions.assertTrue(errors.getFirst().contains("99"), errors.getFirst());
    }

    private static List<String> captureCypherStatements(Runnable action) {
        Logger logger = (Logger) LoggerFactory.getLogger("org.springframework.data.neo4j.cypher");
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
        return appender.list.stream().filter(event -> event.getLevel() == Level.DEBUG).map(ILoggingEvent::getFormattedMessage).toList();
    }

    private static List<ILoggingEvent> captureErrors(Runnable action) {
        Logger logger = (Logger) LoggerFactory.getLogger("io.github.ardoco.core.neo4jschema");
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            action.run();
        } finally {
            logger.detachAppender(appender);
        }
        return appender.list;
    }

    private static List<String> errorMessages(List<ILoggingEvent> events) {
        return events.stream().filter(event -> event.getLevel() == Level.ERROR).map(ILoggingEvent::getFormattedMessage).toList();
    }
}
