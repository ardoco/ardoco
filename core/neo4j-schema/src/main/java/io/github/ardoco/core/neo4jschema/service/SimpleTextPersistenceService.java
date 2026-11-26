/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.text.PlainSimpleText;
import edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText;
import io.github.ardoco.core.neo4jschema.entities.documentation.SimpleTextNode;
import io.github.ardoco.core.neo4jschema.repository.documentation.SimpleTextRepository;

@Service
public class SimpleTextPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleTextPersistenceService.class);

    private final SimpleTextRepository repository;
    private final Neo4jClient neo4jClient;

    public SimpleTextPersistenceService(SimpleTextRepository repository, Neo4jClient neo4jClient) {
        this.repository = repository;
        this.neo4jClient = neo4jClient;
    }

    @Transactional
    public void saveSimpleText(SimpleText simpleText, String identifier) {
        repository.deleteByIdentifier(identifier);
        SimpleTextNode node = new SimpleTextNode(identifier);
        node.setText(simpleText.getText());
        List<String> lines = new ArrayList<>();
        simpleText.getLines().forEach(lines::add);
        node.setLines(lines);
        repository.save(node);
        persistLineSentences(identifier, lines);
        logger.debug("Saved SimpleText {}", identifier);
    }

    private void persistLineSentences(String identifier, List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        List<Map<String, Object>> items = new ArrayList<>(lines.size());
        List<String> sentenceIds = new ArrayList<>(lines.size());
        for (int i = 0; i < lines.size(); i++) {
            String ardocoId = "simpletext-sentence-" + i;
            items.add(Map.of("ardocoId", ardocoId, "sentenceNumber", i, "text", lines.get(i)));
            sentenceIds.add(ardocoId);
        }
        neo4jClient.query("""
                UNWIND $items AS item
                CREATE (n:Sentence:Traceable)
                SET n += item
                """).bind(items).to("items").run();
        neo4jClient.query("""
                MATCH (t:SimpleText {identifier: $identifier})
                UNWIND $ids AS sId
                MATCH (s:Sentence {ardocoId: sId})
                CREATE (t)-[:HAS_SENTENCE]->(s)
                """).bindAll(Map.of("identifier", identifier, "ids", sentenceIds)).run();
    }

    @Transactional(readOnly = true)
    public boolean hasSimpleText(String identifier) {
        return repository.existsByIdentifier(identifier);
    }

    @Transactional(readOnly = true)
    public Optional<SimpleText> loadSimpleText(String identifier) {
        return repository.findByIdentifier(identifier).map(node -> new PlainSimpleText(node.getText(), node.getLines()));
    }

    @Transactional
    public void deleteSimpleText(String identifier) {
        repository.deleteByIdentifier(identifier);
    }
}
