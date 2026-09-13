/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public SimpleTextPersistenceService(SimpleTextRepository repository) {
        this.repository = repository;
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
        logger.debug("Saved SimpleText {}", identifier);
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
