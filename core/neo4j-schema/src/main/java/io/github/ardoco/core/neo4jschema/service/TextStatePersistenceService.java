/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * Write-only persistence of TextState noun mappings.
 * Links to existing {@code Word} / {@code Phrase} nodes created during text preprocessing.
 */
@Service
public class TextStatePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(TextStatePersistenceService.class);

    private static final String UPSERT_NODE = """
            MERGE (nm:NounMapping {ardocoId: $ardocoId})
            SET nm.reference = $reference,
                nm.kind = $kind,
                nm.probability = $probability,
                nm.isCompound = $isCompound,
                nm.surfaceForms = $surfaceForms,
                nm.nameProbability = $nameProbability,
                nm.typeProbability = $typeProbability
            WITH nm
            OPTIONAL MATCH (nm)-[old:MAPS_WORD|HAS_REFERENCE_WORD|IN_PHRASE]->()
            DELETE old
            """;

    private static final String LINK_WORDS = """
            MATCH (nm:NounMapping {ardocoId: $ardocoId})
            UNWIND $positions AS pos
            MATCH (w:Word {position: pos})
            MERGE (nm)-[:MAPS_WORD]->(w)
            """;

    private static final String LINK_REFERENCE_WORDS = """
            MATCH (nm:NounMapping {ardocoId: $ardocoId})
            UNWIND $positions AS pos
            MATCH (w:Word {position: pos})
            MERGE (nm)-[:HAS_REFERENCE_WORD]->(w)
            """;

    /**
     * Links a {@code NounMapping} to the {@code Phrase} whose direct {@code CONTAINS_WORD} positions match
     * {@link Phrase#getContainedWords()} exactly. {@code text} alone is not unique across a document; word
     * positions are the same stable keys used for {@code Word} nodes during text preprocessing.
     */
    private static final String LINK_PHRASE_BY_WORD_POSITIONS = """
            MATCH (nm:NounMapping {ardocoId: $ardocoId})
            WITH nm, $positions AS positions, $phraseType AS phraseType
            WHERE size(positions) > 0
            MATCH (p:Phrase {phraseType: phraseType})
            MATCH (p)-[:CONTAINS_WORD]->(w:Word)
            WITH nm, p, positions, collect(DISTINCT w.position) AS phrasePositions
            WHERE size(phrasePositions) = size(positions)
              AND ALL(pos IN positions WHERE pos IN phrasePositions)
            MERGE (nm)-[:IN_PHRASE]->(p)
            """;

    private static final String DELETE_NODE = """
            MATCH (nm:NounMapping {ardocoId: $ardocoId})
            DETACH DELETE nm
            """;

    private final Neo4jClient neo4jClient;

    public TextStatePersistenceService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    @Transactional
    public void saveNounMapping(NounMapping mapping) {
        String ardocoId = mapping.getArdocoId();
        List<Integer> wordPositions = mapping.getWords().collect(Word::getPosition).toList();
        List<Integer> referencePositions = mapping.getReferenceWords().collect(Word::getPosition).toList();
        List<String> surfaceForms = mapping.getSurfaceForms().toList();

        neo4jClient.query(UPSERT_NODE)
                .bind(ardocoId)
                .to("ardocoId")
                .bind(mapping.getReference())
                .to("reference")
                .bind(mapping.getKind().name())
                .to("kind")
                .bind(mapping.getProbability())
                .to("probability")
                .bind(mapping.isCompound())
                .to("isCompound")
                .bind(surfaceForms)
                .to("surfaceForms")
                .bind(mapping.getProbabilityForKind(MappingKind.NAME))
                .to("nameProbability")
                .bind(mapping.getProbabilityForKind(MappingKind.TYPE))
                .to("typeProbability")
                .run();

        if (!wordPositions.isEmpty()) {
            neo4jClient.query(LINK_WORDS).bind(ardocoId).to("ardocoId").bind(wordPositions).to("positions").run();
        }
        if (!referencePositions.isEmpty()) {
            neo4jClient.query(LINK_REFERENCE_WORDS).bind(ardocoId).to("ardocoId").bind(referencePositions).to("positions").run();
        }
        for (Phrase phrase : mapping.getPhrases()) {
            List<Integer> phraseWordPositions = phrase.getContainedWords().collect(Word::getPosition).toList();
            if (phraseWordPositions.isEmpty()) {
                logger.warn("Skipping IN_PHRASE for NounMapping {}: phrase has no contained words to match in Neo4j", ardocoId);
                continue;
            }
            long linked = neo4jClient.query(LINK_PHRASE_BY_WORD_POSITIONS)
                    .bind(ardocoId)
                    .to("ardocoId")
                    .bind(phraseWordPositions)
                    .to("positions")
                    .bind(phrase.getPhraseType().name())
                    .to("phraseType")
                    .run()
                    .counters()
                    .relationshipsCreated();
            if (linked == 0) {
                logger.warn("No Phrase matched for NounMapping {} (type={}, positions={})", ardocoId, phrase.getPhraseType(), phraseWordPositions);
            }
        }
        logger.debug("Saved NounMapping {} ({} words)", ardocoId, wordPositions.size());
    }

    @Transactional
    public void deleteNounMapping(String ardocoId) {
        neo4jClient.query(DELETE_NODE).bind(ardocoId).to("ardocoId").run();
        logger.debug("Deleted NounMapping {}", ardocoId);
    }
}
