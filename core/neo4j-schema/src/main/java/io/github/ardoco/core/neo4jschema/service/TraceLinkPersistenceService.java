/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeItemNode;
import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkType;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TransitiveChainQueryResult;
import io.github.ardoco.core.neo4jschema.mapper.ArchitectureModelMapper;
import io.github.ardoco.core.neo4jschema.mapper.CodeModelMapper;
import io.github.ardoco.core.neo4jschema.repository.tracelink.TraceLinkRepository;

@Service
public class TraceLinkPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(TraceLinkPersistenceService.class);

    private final TraceLinkRepository traceLinkRepo;
    private final DocumentationPersistenceService documentationService;

    private final Neo4jClient neo4jClient;
    private final Neo4jMappingContext mappingContext;

    private final ArchitectureModelMapper archMapper;
    private final CodeModelMapper codeMapper;

    public TraceLinkPersistenceService(Neo4jClient neo4jClient, Neo4jMappingContext mappingContext, TraceLinkRepository traceLinkRepo,
            ArchitectureModelMapper archMapper, CodeModelMapper codeMapper, DocumentationPersistenceService documentationService) {
        this.traceLinkRepo = traceLinkRepo;
        this.archMapper = archMapper;
        this.codeMapper = codeMapper;
        this.documentationService = documentationService;
        this.neo4jClient = neo4jClient;
        this.mappingContext = mappingContext;
    }

    /**
     * Deletes all trace links of a specific type from the database. TraceLinkType. If no links of the specified type exist, it does nothing.
     *
     * @param type The TraceLinkType for which all corresponding trace links should be deleted from the database.
     */
    @Transactional
    public void deleteTraceLinksByType(TraceLinkType type) {
        traceLinkRepo.deleteLinksByType(type);
    }

    /**
     * Deletes all trace links from the database, regardless of their type.
     */
    @Transactional
    public void deleteAllTraceLinks() {
        traceLinkRepo.deleteAllTraceLinks();
    }

    /**
     * Saves a collection of trace links to the database. It assumes that the elements between which the links exist are already present in the database. For
     * each trace link, it determines the appropriate TraceLinkType based on the link's class and the types of its endpoints, then creates and saves the
     * corresponding relationships in the Neo4j database. If a trace link cannot be mapped to a known type, it is skipped.
     *
     * @param traceLinks A collection of trace links to be saved into the database. This collection can contain different types of trace links, such as
     *                   ArchitectureCodeTraceLink, SentenceModelTraceLink or TransitiveTracelink
     * @return true if the trace links were processed (even if some were skipped due to unknown types)
     */
    public boolean saveTracelinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
        for (TraceLink<?, ?> link : traceLinks) {
            resolveAndSave(link);
        }
        return true;
    }

    private void resolveAndSave(TraceLink<?, ?> link) {
        if (link instanceof TransitiveTraceLink<?, ?> transitive) {
            saveAtomicLink(transitive.getFirstTraceLink(), TraceLinkType.SENTENCE_ARCHITECTURE);
            saveAtomicLink(transitive.getSecondTraceLink(), TraceLinkType.ARCHITECTURE_CODE);
            return;
        }

        TraceLinkType type = determineType(link);
        if (type != null) {
            saveAtomicLink(link, type);
        }
    }

    private TraceLinkType determineType(TraceLink<?, ?> link) {
        if (link instanceof ArchitectureCodeTraceLink)
            return TraceLinkType.ARCHITECTURE_CODE;
        if (link instanceof SentenceModelTraceLink smtl) {
            return (smtl.getSecondEndpoint() instanceof ArchitectureItem) ? TraceLinkType.SENTENCE_ARCHITECTURE : TraceLinkType.SENTENCE_CODE;
        }
        return null;
    }

    private String getEndpointId(Object endpoint) {
        if (endpoint instanceof SentenceEntity se) {
            Sentence s = se.getSentence();
            return s.getSentenceNumber() + String.valueOf(s.getText().hashCode());
        }
        if (endpoint instanceof ModelEntity me)
            return me.getId();
        return endpoint.toString();
    }

    private void saveAtomicLink(TraceLink<?, ?> link, TraceLinkType type) {
        String sourceId = getEndpointId(link.getFirstEndpoint());
        String targetId = getEndpointId(link.getSecondEndpoint());
        traceLinkRepo.createTraceLink(sourceId, targetId, type);
    }

    // ------------- Loading Tracelinks ----------------------------

    /**
     * Generic helper to load links from the graph and map them.
     */
    private <S extends TraceableNode, T> Set<T> loadLinks(TraceLinkType type, Class<S> sourceClass, BiFunction<S, TraceableNode, T> mapper) {
        return traceLinkRepo.findAllByRelationshipType(type)
                .stream()
                .filter(sourceClass::isInstance)
                .map(sourceClass::cast)
                .flatMap(sourceNode -> sourceNode.getOutgoingLinks()
                        .stream()
                        .filter(rel -> rel.getTraceLinkType() == type)
                        .map(rel -> mapper.apply(sourceNode, rel.getTargetNode()))
                        .filter(Objects::nonNull))
                .collect(Collectors.toSet());
    }

    /**
     * Specialized helper for sentence-based links that requires preprocessed text.
     */
    private <S extends TraceableNode> Set<SentenceModelTraceLink> loadSentenceLinks(String textId, TraceLinkType type, Class<S> targetClass,
            BiFunction<SentenceEntity, TraceableNode, SentenceModelTraceLink> mapper) {
        Text domainText = documentationService.loadPreprocessedText(textId).orElse(null);
        if (domainText == null) {
            logger.info("Could not find domain text for ID: {}", textId);
            return new HashSet<>();
        }
        return loadLinks(type, SentenceNode.class, (sNode, targetNode) -> {
            if (targetClass.isInstance(targetNode)) {
                Sentence domainSentence = domainText.getSentences().detect(s -> s.getSentenceNumber() == sNode.getSentenceNumber());
                if (domainSentence != null) {
                    return mapper.apply(new SentenceEntity(domainSentence), targetNode);
                }
            }
            return null;
        });

    }

    /**
     * Loads all ArchitectureCodeTraceLinks from the database. It retrieves all trace links of type ARCHITECTURE_CODE, maps the source and target nodes to their
     * corresponding domain entities using the provided mappers, and returns a set of ArchitectureCodeTraceLink objects.
     *
     * @return A set of ArchitectureCodeTraceLink objects representing all architecture-code trace links currently stored in the database. If no such links
     * exist, it returns an empty set.
     */
    @Transactional(readOnly = true)
    public Set<ArchitectureCodeTraceLink> loadAllArchitectureCodeTraceLinks() {
        return loadLinks(TraceLinkType.ARCHITECTURE_CODE, ArchitectureItemNode.class, (archNode, target) -> {
            if (target instanceof CodeItemNode codeNode) {
                return new ArchitectureCodeTraceLink(archMapper.mapItem(archNode), codeMapper.toDomain(codeNode));
            }
            return null;
        });
    }

    /**
     * Loads all SentenceModelTraceLinks of type SENTENCE_ARCHITECTURE from the database for the given text ID. Note: the loaded architecture items won't have
     * their relationships to other architecture items loaded, as this method is optimized for loading sentence links and not for reconstructing the full
     * architecture model.
     *
     * @param textId The identifier of the preprocessed text for which the sentence-architecture trace links should be loaded. This ID is used to retrieve the
     *               corresponding Text object, which is necessary to map the SentenceNodes to SentenceEntities. If no Text is found for the given ID, it
     *               returns an empty set.
     * @return A set of SentenceModelTraceLink objects representing all sentence-architecture trace links currently stored in the database.
     */
    @Transactional(readOnly = true)
    public Set<SentenceModelTraceLink> loadAllSentenceArchitectureModelTraceLinks(String textId) {
        return loadSentenceLinks(textId, TraceLinkType.SENTENCE_ARCHITECTURE, ArchitectureItemNode.class,
                (sEntity, target) -> new SentenceModelTraceLink(sEntity, archMapper.mapItem((ArchitectureItemNode) target)));
    }

    /**
     * Loads all SentenceModelTraceLinks of type SENTENCE_CODE from the database for the given text ID. Note: the loaded code items won't have their
     * relationships to other architecture items loaded, as this method is optimized for loading sentence links and not for reconstructing the full architecture
     * model.
     *
     * @param textId The identifier of the preprocessed text for which the sentence-architecture trace links should be loaded. This ID is used to retrieve the
     *               corresponding Text object, which is necessary to map the SentenceNodes to SentenceEntities. If no Text is found for the given ID, it
     *               returns an empty set.
     * @return A set of SentenceModelTraceLink objects representing all sentence-code trace links currently stored in the database.
     */
    @Transactional(readOnly = true)
    public Set<SentenceModelTraceLink> loadAllSentenceCodeModelTraceLinks(String textId) {
        return loadSentenceLinks(textId, TraceLinkType.SENTENCE_CODE, CodeItemNode.class,
                (sEntity, target) -> new SentenceModelTraceLink(sEntity, codeMapper.toDomain((CodeItemNode) target)));
    }

    /**
     * Loads all transitive trace links from sentences over architecture items to code items from the database for the given text ID. This method executes a
     * custom Cypher query to retrieve all chains of trace links that connect sentences to architecture items and then to code items.
     *
     * @param textId The identifier of the preprocessed text for which the transitive trace links should be loaded. This ID is used to retrieve the
     *               corresponding Text object, which is necessary to map the SentenceNodes to SentenceEntities. If no Text is found for the given ID, it
     *               returns an empty set.
     * @return A set of TransitiveTraceLink objects representing all transitive trace links from sentences to architecture items to code items currently stored
     * in the database. If no such links exist, it returns an empty set.
     */
    @Transactional(readOnly = true)
    public Set<TraceLink<SentenceEntity, ? extends ModelEntity>> loadTransitiveTraceLinks(String textId) {
        Text domainText = documentationService.loadPreprocessedText(textId).orElse(null);

        if (domainText == null) {
            logger.info("Could not find domain text for ID: {}. Cannot load transitiveTraceLinks", textId);
            return new HashSet<>();
        }

        var sentenceMapper = mappingContext.getRequiredMappingFunctionFor(SentenceNode.class);
        var traceableMapper = mappingContext.getRequiredMappingFunctionFor(TraceableNode.class);

        return neo4jClient.query("""
                        MATCH (s:Sentence)-[r1:TRACES_TO]->(mid:Traceable)-[r2:TRACES_TO]->(end:Traceable)
                        WHERE r1.traceLinkType = $type1 AND r2.traceLinkType = $type2
                        RETURN DISTINCT s, mid, end
                        """)
                .bind(TraceLinkType.SENTENCE_ARCHITECTURE.name())
                .to("type1")
                .bind(TraceLinkType.ARCHITECTURE_CODE.name())
                .to("type2")
                .fetchAs(TransitiveChainQueryResult.class)
                .mappedBy((typeSystem, record) -> new TransitiveChainQueryResult(sentenceMapper.apply(typeSystem, record.get("s")),
                        traceableMapper.apply(typeSystem, record.get("mid")), traceableMapper.apply(typeSystem, record.get("end"))))
                .all()
                .stream()
                .map(chain -> createTransitiveLink(chain, domainText))
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    private Optional<TransitiveTraceLink<SentenceEntity, ? extends ModelEntity>> createTransitiveLink(TransitiveChainQueryResult chain, Text text) {
        Sentence domainSentence = text.getSentences().detect(s -> s.getSentenceNumber() == chain.getSentence().getSentenceNumber());
        if (domainSentence == null)
            return Optional.empty();

        SentenceEntity sentenceEntity = new SentenceEntity(domainSentence);
        ArchitectureItem archMid = mapToArchitectureItem(chain.getArchitecture());
        CodeItem codeEnd = mapToCodeItem(chain.getCode());

        var transitiveLink = TransitiveTraceLink.createTransitiveTraceLink(new SentenceModelTraceLink(sentenceEntity, archMid),
                new ArchitectureCodeTraceLink(archMid, codeEnd));

        return transitiveLink.map(link -> (TransitiveTraceLink<SentenceEntity, ? extends ModelEntity>) link);
    }

    /**
     * Helper to handle the polymorphic mapping of TraceableNodes to Domain Entities.
     */
    private ArchitectureItem mapToArchitectureItem(TraceableNode node) {
        if (node instanceof ArchitectureItemNode archNode) {
            return archMapper.mapItem(archNode);
        }
        throw new IllegalStateException("Unexpected node type in transitive chain: " + node.getClass() + ", expected ArchitectureItemNode");
    }

    /**
     * Helper to handle the polymorphic mapping of TraceableNodes to Domain Entities.
     */
    private CodeItem mapToCodeItem(TraceableNode node) {
        if (node instanceof CodeItemNode codeNode) {
            return codeMapper.toDomain(codeNode);
        }
        throw new IllegalStateException("Unexpected node type in transitive chain: " + node.getClass() + ", expected CodeItemNode");
    }
}
