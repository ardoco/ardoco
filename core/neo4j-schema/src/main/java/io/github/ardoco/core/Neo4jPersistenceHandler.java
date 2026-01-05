package io.github.ardoco.core;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import io.github.ardoco.core.adapter.Neo4jText;
import io.github.ardoco.core.entities.architectureModel.ArchitectureModelNode;
import io.github.ardoco.core.entities.codeModel.CodeModelNode;
import io.github.ardoco.core.entities.documentation.TextNode;
import io.github.ardoco.core.repository.architectureModel.ArchitectureModelRepository;
import io.github.ardoco.core.repository.codeModel.CodeModelRepository;
import io.github.ardoco.core.repository.documentation.TextNodeRepository;
import io.github.ardoco.core.service.architectureModel.ArchitecturePersistenceService;
import io.github.ardoco.core.service.codeModel.CodePersistenceService;
import io.github.ardoco.core.service.documentation.DocumentationPersistenceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Service
public class Neo4jPersistenceHandler implements PersistenceHandler {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jPersistenceHandler.class);

    private final TextNodeRepository textNodeRepository;
    private final DocumentationPersistenceService documentationService;
    private final ArchitecturePersistenceService architectureService;
    private final ArchitectureModelRepository architectureRepository;
    private final CodePersistenceService codePersistenceService;
    private final CodeModelRepository codeModelRepository;

    public Neo4jPersistenceHandler(
            TextNodeRepository textNodeRepository,
            DocumentationPersistenceService documentationService,
            ArchitecturePersistenceService architectureService,
            ArchitectureModelRepository architectureRepository,
            CodePersistenceService codePersistenceService,
            CodeModelRepository codeModelRepository) {
        this.textNodeRepository = textNodeRepository;
        this.documentationService = documentationService;
        this.architectureService = architectureService;
        this.architectureRepository = architectureRepository;
        this.codePersistenceService = codePersistenceService;
        this.codeModelRepository = codeModelRepository;
    }

    @Override
    public void saveModel(Metamodel metamodel, Model model) {
        if (metamodel.isArchitectureModel() && model instanceof ArchitectureModel architectureModel) {
            architectureService.saveArchitectureModel(architectureModel);
            logger.info("Architecture Model saved successfully");

        } else if (metamodel.isCodeModel() && model instanceof CodeModel codeModel) {
            codePersistenceService.saveCodeModel(codeModel);
            logger.info("Code Model saved successfully");

        } else {
            logger.warn("Unknown model type for persistence: " + model.getClass().getName());
        }
    }

    @Override
    public Model loadModel(Metamodel metamodel) {
        if (metamodel.isCodeModel()) {
            return loadCodeModel(metamodel);
        } else if (metamodel.isArchitectureModel()) {
            return loadArchitectureModel(metamodel);
        } else {
            logger.warn("Unknown metamodel type for loading: " + metamodel);
            return null;
        }
    }
    private Model loadCodeModel(Metamodel metamodel) {
        List<CodeModelNode> nodes = codeModelRepository.findAll();
        logger.info("Found " + nodes.size() + " code models in DB.");
        assert nodes.size() <= 1;
        for (CodeModelNode node : nodes) {
            if (node.getMetamodel().equals(metamodel.name())) {
                logger.info("Loading Code Model from DB: " + node.getModelId());
                return codePersistenceService.loadCodeModel(node.getModelId());
            }
        }
        logger.warn("No Code Model found for type: " + metamodel);
        return null;
    }
    private Model loadArchitectureModel(Metamodel metamodel) {
        List<ArchitectureModelNode> nodes = architectureRepository.findAll();
        logger.info("Found " + nodes.size() + " architecture models in DB.");
        assert nodes.size() <= 1;
        for (ArchitectureModelNode node : nodes) {
            if (node.getMetamodel() != null && node.getMetamodel().equals(metamodel.name())) {
                logger.info("Loading Architecture Model from DB: " + node.getModelId());
                return architectureService.loadArchitectureModel(node.getModelId());
            }
        }
        logger.warn("No Architecture Model found for type: " + metamodel);
        return null;
    }


    @Override
    public SortedSet<Metamodel> getStoredMetamodels() {
        SortedSet<Metamodel> available = new TreeSet<>();
        List<ArchitectureModelNode> archNodes = architectureRepository.findAll();
        for (ArchitectureModelNode node : archNodes) {
            try {
                if (node.getMetamodel() != null) {
                    available.add(Metamodel.valueOf(node.getMetamodel()));
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Found unknown Metamodel string in DB: " + node.getMetamodel());
            }
        }

        return available;
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
