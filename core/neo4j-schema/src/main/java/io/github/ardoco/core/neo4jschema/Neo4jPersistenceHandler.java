/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;

import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;

import io.github.ardoco.core.neo4jschema.service.InconsistencyPersistenceService;

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

/**
 * Neo4j-based implementation of the PersistenceHandler interface. Delegates to specific services for each model type and trace links.
 */
@Service
public class Neo4jPersistenceHandler implements PersistenceHandler {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jPersistenceHandler.class);

    private final DocumentationPersistenceService documentationService;
    private final ArchitecturePersistenceService architectureService;
    private final CodePersistenceService codeService;
    private final TraceLinkPersistenceService traceLinkService;
    private final InconsistencyPersistenceService inconsistencyService;

    public Neo4jPersistenceHandler(DocumentationPersistenceService documentationService, ArchitecturePersistenceService architectureService,
            CodePersistenceService codeService, TraceLinkPersistenceService traceLinkService,
            InconsistencyPersistenceService inconsistencyService) {
        this.documentationService = documentationService;
        this.architectureService = architectureService;
        this.codeService = codeService;
        this.traceLinkService = traceLinkService;
        this.inconsistencyService = inconsistencyService;
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
        logger.info("Saving ArchitectureCodeTraceLinks");
        return this.traceLinkService.saveTracelinks(traceLinks);
    }

    @Override
    public Collection<ArchitectureCodeTraceLink> loadSamCodeTraceLinks() {
        logger.info("Loading ArchitectureCodeTraceLinks");
        Set<ArchitectureCodeTraceLink> links =  this.traceLinkService.loadAllArchitectureCodeTraceLinks();
        logger.info("Loaded {} ArchitectureCodeTraceLinks", links.size());
        return links;
    }

    @Override
    public boolean saveTransitiveTraceLinks(Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks) {

        // save real transitive tracelinks
        Set<TransitiveTraceLink<SentenceEntity, ? extends ModelEntity>> transitiveTraceLinks = traceLinks.stream()
                .filter(TransitiveTraceLink.class::isInstance)
                .map(link -> (TransitiveTraceLink<SentenceEntity, ? extends ModelEntity>) link)
                .collect(Collectors.toSet());
        logger.info("Saving {} unique TransitiveTracelinks", transitiveTraceLinks.size());
        this.traceLinkService.saveTransitiveTracelinks(transitiveTraceLinks);

        Set<SentenceModelTraceLink> sentenceModelTraceLinks = traceLinks.stream()
                .filter(SentenceModelTraceLink.class::isInstance)
                .map(link -> (SentenceModelTraceLink) link)
                .filter(link -> link.getSecondEndpoint() instanceof CodeItem)
                .collect(Collectors.toSet());
        logger.info("Saving {} unique SentenceCodeModelTracelinks", sentenceModelTraceLinks.size());
        this.traceLinkService.saveTracelinks(sentenceModelTraceLinks);
        return true;
    }

    @Override
    public Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> loadTransitiveTraceLinks() {
        logger.info("Loading TransitiveTracelinks");
        Set<TraceLink<SentenceEntity, ? extends ModelEntity>> transitiveLinks =  this.traceLinkService.loadTransitiveTraceLinks();
        logger.info("Loaded {} TransitiveTraceLinks", transitiveLinks.size());
        Set<SentenceModelTraceLink> directLinks = this.loadSentenceModelTraceLinks();
        logger.info("Loaded {} SentenceCodeModelTraceLinks", directLinks.size());
        transitiveLinks.addAll(directLinks);
        return transitiveLinks;
    }

    @Override
    public Set<SentenceModelTraceLink> loadSentenceModelTraceLinks() {
        logger.info("Loading SentenceModelTraceLinks");
        Set<SentenceModelTraceLink> links =  this.traceLinkService.loadAllSentenceArchitectureModelTraceLinks();
        logger.info("Loaded {} SentenceArchitectureModelTraceLinks", links.size());
        Set<SentenceModelTraceLink> codeLinks =  this.traceLinkService.loadAllSentenceCodeModelTraceLinks();
        logger.info("Loaded {} SentenceCodeModelTraceLinks", codeLinks.size());
        links.addAll(codeLinks);
        logger.info("=> Loaded {} SentenceModelTraceLinks in total.", links.size());
        return links;
    }

    @Override
    public boolean saveSentenceModelTraceLinks(Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> traceLinks) {
        Set<TraceLink<SentenceEntity, ? extends ModelEntity>> uniqueLinks = new HashSet<>(traceLinks);
        logger.info("Saving {} SentenceModelTracelinks", uniqueLinks.size());
        return this.traceLinkService.saveTracelinks(uniqueLinks);
    }

    @Override
    public boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
        logger.info("Saving {} inconsistencies", inconsistencies.size());
        return this.inconsistencyService.addInconsistencies(inconsistencies);
    }

    @Override
    public Collection<? extends Inconsistency> getInconsistencies() {
        Collection<? extends Inconsistency> inconsistencies = this.inconsistencyService.getInconsistencies();
        logger.info("Loaded {} inconsistencies", inconsistencies.size());
        return inconsistencies;
     }

}
