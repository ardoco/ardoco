/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedMaps;
import org.eclipse.collections.api.factory.SortedSets;
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
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceBridge;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.tlr.textextraction.NounMappingImpl;
import io.github.ardoco.core.neo4jschema.util.models.TextFactory;

@SpringBootTest(classes = Main.class)
class NounMappingBatchWriteTest extends AbstractNeo4jTest {

    private static final Claimant TEST_CLAIMANT = new Claimant() {
    };
    private static final int DELETED = 2100;
    private static final int KEPT = 5;

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void setUp() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @AfterEach
    void tearDown() {
        PersistenceBridge.setHandler(null);
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.empty());
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    @DisplayName("More than 2000 noun-mapping deletions in one stage flush take at most 2 delete queries and leave the same graph as one-by-one deletes")
    void deletionsAreBatched() {
        List<String> deletedIds = new ArrayList<>();
        for (int i = 0; i < DELETED; i++) {
            deletedIds.add("batch-delete-nm-" + i);
        }

        createFixture();
        for (String id : deletedIds) {
            persistenceHandler.deleteNounMapping(id);
        }
        Snapshot oneByOne = snapshot();

        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
        createFixture();
        PersistenceBridge.setHandler(persistenceHandler);
        PersistenceBridge.getInstance().applyConfiguration(SortedMaps.immutable.with("PersistenceBridge::storage", "neo4j"));
        List<String> statements = captureCypherStatements(() -> PersistenceBridge.withDeferredWrites("stage-delete", () -> {
            deletedIds.forEach(PersistenceBridge::deleteNounMapping);
            return null;
        }));

        long deleteQueries = statements.stream().filter(statement -> statement.contains("DETACH DELETE nm")).count();
        Assertions.assertTrue(deleteQueries <= 2, deleteQueries + " delete queries");
        Assertions.assertEquals(oneByOne, snapshot());
        Assertions.assertEquals(KEPT, oneByOne.nounMappings().size());
    }

    @Test
    @DisplayName("Saving N noun mappings with phrases needs the same number of phrase lookups for N=1 and N=4, with the expected IN_PHRASE edges")
    void phraseLookupsDoNotGrowWithTheNumberOfMappings() {
        Text text = TextFactory.createComplexText("batch_phrase_text");
        persistenceHandler.savePreprocessedText(text, PreprocessingData.ID);
        List<Word> words = text.getSentences().get(0).getWords().toList();
        List<NounMapping> one = List.of(nounMapping(words.get(1)));
        List<NounMapping> four = List.of(nounMapping(words.get(1)), nounMapping(words.get(2)), nounMapping(words.get(0)), nounMapping(words.get(1), words.get(
                2)));

        long lookupsForOne = phraseLookups(captureCypherStatements(() -> persistenceHandler.saveNounMappings(one)));
        long lookupsForFour = phraseLookups(captureCypherStatements(() -> persistenceHandler.saveNounMappings(four)));

        Assertions.assertTrue(lookupsForOne > 0, "the phrase lookup must be captured");
        Assertions.assertEquals(lookupsForOne, lookupsForFour, "phrase lookups for 1 vs 4 noun mappings");
        for (NounMapping mapping : four) {
            Assertions.assertEquals(expectedPhrases(mapping), storedPhrases(mapping.getArdocoId()), mapping.getArdocoId());
        }
    }

    @Test
    @DisplayName("A saved noun mapping keeps every stored property and its word edges equal to the domain object")
    void savedNounMappingKeepsEveryProperty() {
        Text text = TextFactory.createComplexText("property_text");
        persistenceHandler.savePreprocessedText(text, PreprocessingData.ID);
        List<Word> words = text.getSentences().get(0).getWords().toList();
        NounMappingImpl mapping = new NounMappingImpl(SortedSets.immutable.with(words.get(1), words.get(2)), MappingKind.NAME, TEST_CLAIMANT, 0.7,
                Lists.immutable.with(words.get(1), words.get(2)), Lists.immutable.with("login component", "Login Component"));
        mapping.addKindWithProbability(MappingKind.TYPE, TEST_CLAIMANT, 0.3);

        persistenceHandler.saveNounMappings(List.of(mapping));

        Map<String, Object> expected = new TreeMap<>(Map.of("ardocoId", mapping.getArdocoId(), "reference", mapping.getReference(), "kind", mapping.getKind()
                .name(), "probability", mapping.getProbability(), "isCompound", mapping.isCompound(), "surfaceForms", new ArrayList<>(mapping.getSurfaceForms()
                        .castToList()), "nameProbability", mapping.getProbabilityForKind(MappingKind.NAME), "typeProbability", mapping.getProbabilityForKind(
                                MappingKind.TYPE)));
        Assertions.assertEquals(expected, storedProperties(mapping.getArdocoId()));
        Assertions.assertEquals(positions(mapping.getWords()), storedWordPositions(mapping.getArdocoId(), "MAPS_WORD"));
        Assertions.assertEquals(positions(mapping.getReferenceWords()), storedWordPositions(mapping.getArdocoId(), "HAS_REFERENCE_WORD"));
    }

    private Map<String, Object> storedProperties(String ardocoId) {
        Map<String, Object> row = neo4jClient.query("MATCH (nm:NounMapping {ardocoId: $id}) RETURN properties(nm) AS p")
                .bind(ardocoId)
                .to("id")
                .fetch()
                .one()
                .orElseThrow();
        Map<String, Object> properties = new TreeMap<>();
        ((Map<?, ?>) row.get("p")).forEach((key, value) -> properties.put((String) key, value));
        return properties;
    }

    private static Set<Integer> positions(Iterable<Word> words) {
        Set<Integer> positions = new TreeSet<>();
        for (Word word : words) {
            positions.add(word.getPosition());
        }
        return positions;
    }

    private Set<Integer> storedWordPositions(String ardocoId, String relationshipType) {
        Set<Integer> positions = new TreeSet<>();
        for (Map<String, Object> row : neo4jClient.query(
                "MATCH (:NounMapping {ardocoId: $id})-[:" + relationshipType + "]->(w:Word) RETURN w.position AS position")
                .bind(ardocoId)
                .to("id")
                .fetch()
                .all()) {
            positions.add(((Number) row.get("position")).intValue());
        }
        return positions;
    }

    private void createFixture() {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < DELETED; i++) {
            ids.add("batch-delete-nm-" + i);
        }
        for (int i = 0; i < KEPT; i++) {
            ids.add("batch-keep-nm-" + i);
        }
        neo4jClient.query("""
                CREATE (w:Word {position: 424242})
                WITH w
                UNWIND $ids AS id
                CREATE (:NounMapping {ardocoId: id})-[:MAPS_WORD]->(w)
                """).bind(ids).to("ids").run();
    }

    private Snapshot snapshot() {
        Set<String> nounMappings = new TreeSet<>(neo4jClient.query("MATCH (nm:NounMapping) RETURN nm.ardocoId AS id")
                .fetchAs(String.class)
                .mappedBy((typeSystem, record) -> record.get("id").asString())
                .all());
        long relationships = neo4jClient.query("MATCH ()-[r]->() RETURN count(r) AS c")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("c").asLong())
                .one()
                .orElse(0L);
        long nodes = neo4jClient.query("MATCH (n) RETURN count(n) AS c")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("c").asLong())
                .one()
                .orElse(0L);
        return new Snapshot(nounMappings, relationships, nodes);
    }

    private static NounMapping nounMapping(Word... words) {
        return new NounMappingImpl(SortedSets.immutable.with(words), MappingKind.NAME, TEST_CLAIMANT, 1.0, Lists.immutable.with(words), Lists.immutable.with(
                words[0].getText()));
    }

    private static Set<String> expectedPhrases(NounMapping mapping) {
        Set<String> expected = new HashSet<>();
        for (Phrase phrase : mapping.getPhrases()) {
            expected.add(phrase.getPhraseType().name() + new TreeSet<>(phrase.getContainedWords().collect(Word::getPosition).toList()));
        }
        return expected;
    }

    private Set<String> storedPhrases(String ardocoId) {
        Set<String> stored = new HashSet<>();
        for (Map<String, Object> row : neo4jClient.query("""
                MATCH (:NounMapping {ardocoId: $id})-[:IN_PHRASE]->(p:Phrase)-[:CONTAINS_WORD]->(w:Word)
                RETURN p.id AS id, p.phraseType AS type, collect(w.position) AS positions
                """).bind(ardocoId).to("id").fetch().all()) {
            List<Integer> positions = new ArrayList<>();
            for (Object position : (List<?>) row.get("positions")) {
                positions.add(((Number) position).intValue());
            }
            stored.add(row.get("type") + new TreeSet<>(positions).toString());
        }
        return stored;
    }

    private static long phraseLookups(List<String> statements) {
        return statements.stream()
                .filter(statement -> statement.contains("Phrase") && statement.contains("CONTAINS_WORD") && !statement.contains("MERGE (nm"))
                .count();
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

    private record Snapshot(Set<String> nounMappings, long relationships, long nodes) {
    }
}
