/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.RecommendationModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.SimpleText;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.common.persistence.PersistenceHandler;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkType;
import io.github.ardoco.core.neo4jschema.service.ArchitecturePersistenceService;
import io.github.ardoco.core.neo4jschema.service.CodePersistenceService;
import io.github.ardoco.core.neo4jschema.service.DocumentationPersistenceService;
import io.github.ardoco.core.neo4jschema.service.InconsistencyPersistenceService;
import io.github.ardoco.core.neo4jschema.service.NerPersistenceService;
import io.github.ardoco.core.neo4jschema.service.ProjectPersistenceService;
import io.github.ardoco.core.neo4jschema.service.RecommendationPersistenceService;
import io.github.ardoco.core.neo4jschema.service.SimpleTextPersistenceService;
import io.github.ardoco.core.neo4jschema.service.TextStatePersistenceService;
import io.github.ardoco.core.neo4jschema.service.TraceLinkPersistenceService;

/**
 * Neo4j-based implementation of the PersistenceHandler interface.
 * Delegates the demands to specific services for each model type and trace links.
 */
@Service
public class Neo4jPersistenceHandler implements PersistenceHandler {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jPersistenceHandler.class);

    private final DocumentationPersistenceService documentationService;
    private final ArchitecturePersistenceService architectureService;
    private final CodePersistenceService codeService;
    private final TraceLinkPersistenceService traceLinkService;
    private final InconsistencyPersistenceService inconsistencyService;
    private final TextStatePersistenceService textStateService;
    private final RecommendationPersistenceService recommendationService;
    private final SimpleTextPersistenceService simpleTextService;
    private final ProjectPersistenceService projectService;
    private final NerPersistenceService nerService;
    private final Neo4jClient neo4jClient;

    public Neo4jPersistenceHandler(DocumentationPersistenceService documentationService, ArchitecturePersistenceService architectureService,
            CodePersistenceService codeService, TraceLinkPersistenceService traceLinkService, InconsistencyPersistenceService inconsistencyService,
            TextStatePersistenceService textStateService, RecommendationPersistenceService recommendationService, SimpleTextPersistenceService simpleTextService,
            ProjectPersistenceService projectService, NerPersistenceService nerService, Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
        this.documentationService = documentationService;
        this.architectureService = architectureService;
        this.codeService = codeService;
        this.traceLinkService = traceLinkService;
        this.inconsistencyService = inconsistencyService;
        this.textStateService = textStateService;
        this.recommendationService = recommendationService;
        this.simpleTextService = simpleTextService;
        this.projectService = projectService;
        this.nerService = nerService;
    }

    @Override
    public void saveModel(Metamodel metamodel, Model model) {
        logger.info("Saving model of type: " + metamodel);
        if (metamodel.isArchitectureModel() && model instanceof ArchitectureModel architectureModel) {
            architectureService.saveArchitectureModel(architectureModel);
        } else if (metamodel.isCodeModel() && model instanceof CodeModel codeModel) {
            codeService.saveCodeModel(codeModel);
        } else {
            throw new IllegalArgumentException("Unknown model type for persistence: " + model.getClass().getName());
        }
    }

    @Override
    public Model loadModel(Metamodel metamodel) {
        logger.info("Loading model of type: " + metamodel);
        if (metamodel.isCodeModel()) {
            return codeService.loadCodeModel(metamodel).orElse(null);
        } else if (metamodel.isArchitectureModel()) {
            return architectureService.loadArchitectureModel(metamodel).orElse(null);
        }
        return null;
    }

    @Override
    public ImmutableSortedSet<Metamodel> getStoredMetamodels() {
        MutableSortedSet<Metamodel> available = SortedSets.mutable.empty();
        available.addAll(codeService.getStoredCodeModelMetamodels());
        available.addAll(architectureService.getStoredArchitectureModelMetamodels());
        return available.toImmutable();
    }

    @Override
    public void savePreprocessedText(Text text, String identifier) {
        logger.info("Saving preprocessed text for " + identifier);
        this.documentationService.savePreprocessedText(text, identifier);
    }

    @Override
    public Text loadPreprocessedText(String identifier) {
        logger.info("Loading preprocessed text for " + identifier);

        Text text = this.documentationService.loadPreprocessedText(identifier).orElse(null);
        if (text == null) {
            logger.warn("No preprocessed text found for identifier: " + identifier);
        }
        return text;
    }

    @Override
    public boolean hasPreprocessedText(String identifier) {
        logger.info("Checking if preprocessed text is available for " + identifier);
        return this.documentationService.hasPreprocessedText(identifier);
    }

    @Override
    public boolean saveTraceLinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
        logger.info("Saving {} Transitive/Sentence-Model/ArchitectureCode Tracelinks", traceLinks.size());
        Set<TraceLink<?, ?>> uniqueLinks = new HashSet<>(traceLinks);
        return this.traceLinkService.saveTracelinks(uniqueLinks);
    }

    @Override
    public Collection<ArchitectureCodeTraceLink> loadArchitectureCodeTraceLinks() {
        logger.info("Loading ArchitectureCodeTraceLinks from neo4j");
        Set<ArchitectureCodeTraceLink> links = this.traceLinkService.loadAllArchitectureCodeTraceLinks();
        logger.info("Loaded {} ArchitectureCodeTraceLinks", links.size());
        return links;
    }

    @Override
    public Collection<? extends TraceLink<SentenceEntity, ? extends ModelEntity>> loadTransitiveTraceLinks() {
        logger.info("Loading TransitiveTraceLinks from neo4j");
        Set<TraceLink<SentenceEntity, ? extends ModelEntity>> transitiveLinks = this.traceLinkService.loadTransitiveTraceLinks();
        logger.info("Loaded {} TransitiveTraceLinks", transitiveLinks.size());
        return transitiveLinks;
    }

    @Override
    public Collection<SentenceModelTraceLink> loadSentenceModelTraceLinks() {
        logger.info("Loading SentenceModelTraceLinks from neo4j");
        Set<SentenceModelTraceLink> links = this.traceLinkService.loadAllSentenceArchitectureModelTraceLinks();
        Set<SentenceModelTraceLink> codeLinks = this.traceLinkService.loadAllSentenceCodeModelTraceLinks();
        links.addAll(codeLinks);
        logger.info("Loaded {} SentenceModelTraceLinks", links.size());
        return links;
    }

    @Override
    public boolean addInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
        logger.info("Saving {} inconsistencies", inconsistencies.size());
        return this.inconsistencyService.addInconsistencies(inconsistencies);
    }

    @Override
    public Collection<? extends Inconsistency> getInconsistencies() {
        logger.info("Loading inconsistencies from neo4j");
        Collection<? extends Inconsistency> inconsistencies = this.inconsistencyService.getInconsistencies();
        logger.info("Loaded {} inconsistencies", inconsistencies.size());
        return inconsistencies;
    }

    @Override
    @Transactional
    public void deleteModel(Metamodel metamodel) {
        logger.info("Starting  deletion for model: {}", metamodel);
        if (metamodel.isArchitectureModel()) {
            inconsistencyService.deleteInconsistenciesOfArchitectureItems();
            traceLinkService.deleteTraceLinksByType(TraceLinkType.ARCHITECTURE_CODE);
            traceLinkService.deleteTraceLinksByType(TraceLinkType.SENTENCE_ARCHITECTURE);
            architectureService.deleteArchitectureModel(metamodel);
        } else if (metamodel.isCodeModel()) {
            inconsistencyService.deleteInconsistenciesOfCodeItems();
            traceLinkService.deleteTraceLinksByType(TraceLinkType.ARCHITECTURE_CODE);
            traceLinkService.deleteTraceLinksByType(TraceLinkType.SENTENCE_CODE);
            codeService.deleteCodeModel(metamodel);
        }
    }

    @Override
    @Transactional
    public void deletePreprocessedText(String identifier) {
        inconsistencyService.deleteTextInconsistencies();
        traceLinkService.deleteTraceLinksByType(TraceLinkType.SENTENCE_CODE);
        traceLinkService.deleteTraceLinksByType(TraceLinkType.SENTENCE_ARCHITECTURE);
        documentationService.deletePreprocessedText(identifier);
        logger.info("Starting cascading deletion for text: {}", identifier);
    }

    @Override
    public void deleteAllData() {
        neo4jClient.query("MATCH (n) DETACH DELETE n ").run();
    }

    @Override
    public void deleteInconsistencies(Collection<? extends Inconsistency> inconsistencies) {
        logger.info("Deleting {} specific inconsistencies", inconsistencies.size());
        inconsistencyService.deleteInconsistencies(inconsistencies);
    }

    @Override
    public void deleteAllInconsistencies() {
        logger.info("Deleting all inconsistencies from persistence");
        inconsistencyService.deleteAllInconsistencies();
    }

    @Override
    public void saveNounMapping(NounMapping nounMapping) {
        logger.debug("Saving NounMapping {}", nounMapping.getArdocoId());
        this.textStateService.saveNounMapping(nounMapping);
    }

    @Override
    public void deleteNounMapping(String ardocoId) {
        logger.debug("Deleting NounMapping {}", ardocoId);
        this.textStateService.deleteNounMapping(ardocoId);
    }

    @Override
    public void saveRecommendedInstance(RecommendedInstance recommendedInstance, Metamodel metamodel) {
        logger.debug("Saving RecommendedInstance {}", recommendedInstance.getId());
        this.recommendationService.saveRecommendedInstance(recommendedInstance, metamodel);
    }

    @Override
    public boolean hasNounMappings() {
        return this.textStateService.hasNounMappings();
    }

    @Override
    public Collection<NounMapping> loadNounMappings(Text annotatedText) {
        logger.info("Loading NounMappings from Neo4j (resume)");
        return this.textStateService.loadAllNounMappings(annotatedText);
    }

    @Override
    public boolean hasRecommendedInstances() {
        return this.recommendationService.hasRecommendedInstances();
    }

    @Override
    public Collection<RecommendedInstance> loadRecommendedInstances(Metamodel metamodel, SortedMap<String, NounMapping> nounMappingsById) {
        logger.info("Loading RecommendedInstances for {} from Neo4j (resume)", metamodel);
        return this.recommendationService.loadRecommendedInstances(metamodel, nounMappingsById);
    }

    @Override
    public boolean hasRecommendationModelTraceLinks() {
        return countRecommendationModelLinks(TraceLinkType.RECOMMENDATION_ARCHITECTURE) > 0
                || countRecommendationModelLinks(TraceLinkType.RECOMMENDATION_CODE) > 0;
    }

    private long countRecommendationModelLinks(TraceLinkType type) {
        return neo4jClient.query("""
                MATCH (:RecommendedInstance)-[r:TRACES_TO]->(:Traceable)
                WHERE r.traceLinkType = $type
                RETURN count(r) AS c
                """)
                .bind(type.name())
                .to("type")
                .fetch()
                .one()
                .map(row -> ((Number) row.get("c")).longValue())
                .orElse(0L);
    }

    @Override
    public Collection<RecommendationModelTraceLink> loadRecommendationModelTraceLinks(SortedMap<String, RecommendedInstance> recommendedInstancesById,
            SortedMap<String, ArchitectureItem> architectureItemsById) {
        logger.info("Loading RecommendationModelTraceLinks (architecture) from Neo4j (resume)");
        return this.traceLinkService.loadAllRecommendationArchitectureTraceLinks(recommendedInstancesById, architectureItemsById);
    }

    @Override
    public Collection<RecommendationModelTraceLink> loadRecommendationCodeTraceLinks(SortedMap<String, RecommendedInstance> recommendedInstancesById,
            SortedMap<String, CodeItem> codeItemsById) {
        logger.info("Loading RecommendationModelTraceLinks (code) from Neo4j (resume)");
        return this.traceLinkService.loadAllRecommendationCodeTraceLinks(recommendedInstancesById, codeItemsById);
    }

    @Override
    public void saveSimpleText(SimpleText simpleText, String identifier) {
        logger.info("Saving SimpleText for {}", identifier);
        this.simpleTextService.saveSimpleText(simpleText, identifier);
    }

    @Override
    public boolean hasSimpleText(String identifier) {
        return this.simpleTextService.hasSimpleText(identifier);
    }

    @Override
    public SimpleText loadSimpleText(String identifier) {
        logger.info("Loading SimpleText for {}", identifier);
        return this.simpleTextService.loadSimpleText(identifier).orElse(null);
    }

    @Override
    public void saveProjectMetadata(String projectName, String inputText) {
        logger.info("Saving Project metadata for {}", projectName);
        this.projectService.saveProjectMetadata(projectName, inputText);
    }

    @Override
    public boolean hasProjectMetadata(String projectName) {
        return this.projectService.hasProjectMetadata(projectName);
    }

    @Override
    public String loadProjectInputText(String projectName) {
        return this.projectService.loadProjectInputText(projectName).orElse(null);
    }

    @Override
    public String loadSoleProjectName() {
        return this.projectService.loadSoleProjectName().orElse(null);
    }

    @Override
    public void saveNamedArchitectureEntity(NamedArchitectureEntity entity, Metamodel metamodel, boolean unlinked) {
        logger.debug("Saving NamedArchitectureEntity {} ({})", entity.getId(), entity.getName());
        this.nerService.saveNamedArchitectureEntity(entity, metamodel, unlinked);
    }

    @Override
    public boolean hasNerNamedArchitectureEntities() {
        return this.nerService.hasNamedArchitectureEntities();
    }

    @Override
    public Collection<NamedArchitectureEntity> loadNamedArchitectureEntities(Metamodel metamodel, boolean unlinkedOnly) {
        logger.info("Loading NamedArchitectureEntities for {} (unlinkedOnly={})", metamodel, unlinkedOnly);
        return this.nerService.loadNamedArchitectureEntities(metamodel, unlinkedOnly);
    }

    @Override
    public Collection<NamedArchitectureEntityToModelTraceLink> loadNerTraceLinks(Metamodel metamodel,
            SortedMap<String, NamedArchitectureEntityOccurrence> occurrencesById, SortedMap<String, ModelEntity> modelEntitiesById) {
        logger.info("Loading NER trace links for {} from Neo4j (resume)", metamodel);
        return this.nerService.loadNerTraceLinks(metamodel, occurrencesById, modelEntitiesById);
    }

}
