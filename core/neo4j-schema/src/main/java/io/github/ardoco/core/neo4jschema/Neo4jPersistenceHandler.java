/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;
import io.github.ardoco.core.neo4jschema.service.TraceLinkPersistenceService;
import io.github.ardoco.core.neo4jschema.service.architectureModel.ArchitecturePersistenceService;
import io.github.ardoco.core.neo4jschema.service.codeModel.CodePersistenceService;
import io.github.ardoco.core.neo4jschema.service.documentation.DocumentationPersistenceService;

@Service
public class Neo4jPersistenceHandler implements PersistenceHandler {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jPersistenceHandler.class);

    private final DocumentationPersistenceService documentationService;
    private final ArchitecturePersistenceService architectureService;
    private final CodePersistenceService codeService;
    private final TraceLinkPersistenceService traceLinkService;

    public Neo4jPersistenceHandler(DocumentationPersistenceService documentationService, ArchitecturePersistenceService architectureService,
            CodePersistenceService codeService, TraceLinkPersistenceService traceLinkService) {
        this.documentationService = documentationService;
        this.architectureService = architectureService;
        this.codeService = codeService;
        this.traceLinkService = traceLinkService;
    }

    @Override
    public void saveModel(Metamodel metamodel, Model model) {
        logger.info("Saving model of type: " + metamodel);
        if (metamodel.isArchitectureModel() && model instanceof ArchitectureModel architectureModel) {
            architectureService.saveArchitectureModel(architectureModel);
        } else if (metamodel.isCodeModel() && model instanceof CodeModel codeModel) {
            codeService.saveCodeModel(codeModel);
        } else {
            logger.warn("Unknown model type for persistence: " + model.getClass().getName());
        }
    }

    @Override
    public Model loadModel(Metamodel metamodel) {
        logger.info("Loading model of type: " + metamodel);
        if (metamodel.isCodeModel()) {
            return codeService.loadCodeModel(metamodel);
        } else if (metamodel.isArchitectureModel()) {
            return architectureService.loadArchitectureModel(metamodel);
        } else {
            logger.warn("Unknown metamodel type for loading: " + metamodel);
            return null;
        }
    }

    @Override
    public SortedSet<Metamodel> getStoredMetamodels() {
        SortedSet<Metamodel> available = new TreeSet<>();
        available.addAll(codeService.getStoredCodeModelMetamodels());
        available.addAll(architectureService.getStoredArchitectureModelMetamodels());
        return available;
    }

    @Override
    public void savePreprocessedText(Text text, String identifier) {
        this.documentationService.savePreprocessedText(text, identifier);
        logger.info("Saving preprocessed text for " + identifier);
    }

    @Override
    public Text loadPreprocessedText(String identifier) {
        logger.info("Loading preprocessed text for " + identifier);
        return this.documentationService.loadPreprocessedText(identifier);
    }

    @Override
    public boolean hasPreprocessedText(String identifier) {
        logger.info("Checking if preprocessed text is available for " + identifier);
        return this.documentationService.hasPreprocessedText(identifier);
    }

    @Override
    public boolean saveSamCodeTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
        logger.info("Saving SamCodeTracelinks");
        return this.traceLinkService.saveAllTraceLinks(traceLinks);
    }

    @Override
    public Collection<ArchitectureCodeTraceLink> loadSamCodeTraceLinks() {
        logger.info("Loading SamCodeTracelinks");
        Set<ArchitectureCodeTraceLink> links =  this.traceLinkService.loadAllArchitectureCodeTraceLinks();
        for (ArchitectureCodeTraceLink architectureCodeTraceLink : links) {
            logger.info("Loaded ArchitectureCodeTraceLink: " + architectureCodeTraceLink);
        }
        return links;
    }

    @Override
    public boolean saveTransitiveTraceLinks(Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks) {
        return false;
    }

    @Override
    public Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> loadTransitiveTraceLinks() {
        return List.of();
    }

}
