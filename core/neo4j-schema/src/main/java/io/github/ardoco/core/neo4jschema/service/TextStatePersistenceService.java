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

    private static final String LINK_PHRASE = """
            MATCH (nm:NounMapping {ardocoId: $ardocoId})
            MATCH (p:Phrase {text: $text, phraseType: $phraseType})
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
            neo4jClient.query(LINK_PHRASE)
                    .bind(ardocoId)
                    .to("ardocoId")
                    .bind(phrase.getText())
                    .to("text")
                    .bind(phrase.getPhraseType().name())
                    .to("phraseType")
                    .run();
        }
        logger.debug("Saved NounMapping {} ({} words)", ardocoId, wordPositions.size());
    }

    @Transactional
    public void deleteNounMapping(String ardocoId) {
        neo4jClient.query(DELETE_NODE).bind(ardocoId).to("ardocoId").run();
        logger.debug("Deleted NounMapping {}", ardocoId);
    }
}
