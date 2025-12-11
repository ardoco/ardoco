package io.github.ardoco.core;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import io.github.ardoco.core.adapter.Neo4jText;
import io.github.ardoco.core.entities.documentation.TextNode;
import io.github.ardoco.core.repository.documentation.TextNodeRepository;
import io.github.ardoco.core.service.documentation.DocumentationPersistenceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Neo4jPersistenceHandler implements PersistenceHandler {

    private final TextNodeRepository textNodeRepository;

    private static final Logger logger = LoggerFactory.getLogger(Neo4jPersistenceHandler.class);


    private final DocumentationPersistenceService documentationService;

    public Neo4jPersistenceHandler(TextNodeRepository textNodeRepository, DocumentationPersistenceService documentationService) {
        this.textNodeRepository = textNodeRepository;
        this.documentationService = documentationService;
    }

    @Override
    public void saveModel(Metamodel metamodel, Model model) {
        // TODO
        return;
    }

    @Override
    public Model loadModel(Metamodel metamodel) {
        // TODO
        return null;
    }

    @Override
    public void savePreprocessedText(Text text, String identifier) {
        this.documentationService.saveDocumentation(text, identifier);
        logger.info("Saving preprocessed text for " + identifier);
    }

    @Override
    public Text loadPreprocessedText(String identifier) {
        logger.info("Loading preprocessed text for " + identifier);
        TextNode textNode = textNodeRepository.findByArdocoId(identifier);
        return new Neo4jText(textNode);
    }

    @Override
    public boolean hasPreprocessedText(String identifier) {
        logger.info("Checking if preprocessed text is available for " + identifier);
        return this.textNodeRepository.existsByArdocoId(identifier);
    }



}
