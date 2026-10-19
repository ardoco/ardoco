/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.io.Serial;
import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.neo4j.core.Neo4jClient;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.PlainSimpleText;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import io.github.ardoco.core.neo4jschema.util.models.ArchitectureModelFactory;
import io.github.ardoco.core.neo4jschema.util.models.CodeModelFactory;

@SpringBootTest(classes = Main.class)
class ArtemisTransitiveLinkPersistenceTest extends AbstractNeo4jTest {

    @Autowired
    private Neo4jPersistenceHandler persistenceHandler;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void clearDatabase() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @AfterEach
    void clearDatabaseAfter() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
    }

    @Test
    @DisplayName("An ArTEMiS-style transitive link's SENTENCE_ARCHITECTURE half is persisted")
    void artemisStyleTransitiveLinkIsPersisted() {
        var simpleText = new PlainSimpleText("Line zero.\nLine one.\nLine two.", List.of("Line zero.", "Line one.", "Line two."));
        persistenceHandler.saveSimpleText(simpleText, "artemis-repro");

        var archModel = ArchitectureModelFactory.createArchitectureModel();
        var codeModel = CodeModelFactory.createCodeModel();
        persistenceHandler.saveModel(Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES, archModel);
        persistenceHandler.saveModel(Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES, codeModel);

        var archEntity = archModel.getEndpoints().get(0);
        var codeItem = codeModel.getEndpoints().get(0);

        int sentenceNumber = 1;
        Sentence syntheticSentence = new Sentence() {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public int getSentenceNumber() {
                return sentenceNumber;
            }

            @Override
            public ImmutableList<Word> getWords() {
                return Lists.immutable.empty();
            }

            @Override
            public String getText() {
                return String.valueOf(sentenceNumber);
            }

            @Override
            public ImmutableList<Phrase> getPhrases() {
                return Lists.immutable.empty();
            }
        };

        var link1 = new SentenceModelTraceLink(syntheticSentence, archEntity);
        var link2 = new ArchitectureCodeTraceLink(archEntity, codeItem);
        var transitiveLink = TransitiveTraceLink.createTransitiveTraceLink(link1, link2)
                .orElseThrow(() -> new IllegalStateException("Failed to create transitive link - IDs didn't match"));

        persistenceHandler.saveTraceLinks(List.of(transitiveLink));

        Long sentenceArchitectureEdges = neo4jClient.query("""
                MATCH ()-[r:TRACES_TO]->() WHERE r.traceLinkType = 'SENTENCE_ARCHITECTURE' RETURN count(r) AS c
                """).fetchAs(Long.class).mappedBy((typeSystem, record) -> record.get("c").asLong()).one().orElse(0L);
        Long architectureCodeEdges = neo4jClient.query("""
                MATCH ()-[r:TRACES_TO]->() WHERE r.traceLinkType = 'ARCHITECTURE_CODE' RETURN count(r) AS c
                """).fetchAs(Long.class).mappedBy((typeSystem, record) -> record.get("c").asLong()).one().orElse(0L);
        Long sentenceNodeCount = neo4jClient.query("MATCH (s:Sentence) RETURN count(s) AS c")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("c").asLong())
                .one()
                .orElse(0L);

        Assertions.assertEquals(1L, sentenceArchitectureEdges, "The transitive link's sentence->architecture half should be persisted as SENTENCE_ARCHITECTURE");
        Assertions.assertEquals(1L, architectureCodeEdges);
        Assertions.assertEquals(3L, sentenceNodeCount);
    }

    @Test
    @DisplayName("Saving the same SimpleText twice leaves exactly one Sentence node per line")
    void resavingSimpleTextDoesNotDuplicateSentenceNodes() {
        var simpleText = new PlainSimpleText("Line zero.\nLine one.\nLine two.", List.of("Line zero.", "Line one.", "Line two."));
        persistenceHandler.saveSimpleText(simpleText, "artemis-resave");
        persistenceHandler.saveSimpleText(simpleText, "artemis-resave");

        Long sentenceNodeCount = neo4jClient.query("MATCH (s:Sentence) RETURN count(s) AS c")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("c").asLong())
                .one()
                .orElse(0L);
        Long hasSentenceEdges = neo4jClient.query("MATCH (:SimpleText)-[r:HAS_SENTENCE]->() RETURN count(r) AS c")
                .fetchAs(Long.class)
                .mappedBy((typeSystem, record) -> record.get("c").asLong())
                .one()
                .orElse(0L);

        Assertions.assertEquals(3L, sentenceNodeCount);
        Assertions.assertEquals(3L, hasSentenceEdges);
    }
}
