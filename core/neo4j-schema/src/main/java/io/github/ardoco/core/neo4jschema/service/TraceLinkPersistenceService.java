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
import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureItemRepository;
import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureModelRepository;
import io.github.ardoco.core.neo4jschema.repository.codeModel.CodeItemRepository;
import io.github.ardoco.core.neo4jschema.repository.tracelink.TraceLinkRepository;
import io.github.ardoco.core.neo4jschema.mapper.ArchitectureModelMapper;
import io.github.ardoco.core.neo4jschema.mapper.CodeModelMapper;

@Service
public class TraceLinkPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(TraceLinkPersistenceService.class);

    private final ArchitectureItemRepository archRepo;
    private final CodeItemRepository codeRepo;
    private final TraceLinkRepository traceLinkRepo;

    private final ArchitectureModelMapper archMapper;
    private final CodeModelMapper codeMapper;

    private final ArchitectureModelRepository archModelRepo;
    private final DocumentationPersistenceService documentationService;

    private final Neo4jClient neo4jClient;
    private final Neo4jMappingContext mappingContext;

    public TraceLinkPersistenceService(Neo4jClient neo4jClient, Neo4jMappingContext mappingContext, ArchitectureItemRepository archRepo,
            ArchitectureModelRepository archModelRepo, CodeItemRepository codeRepo, TraceLinkRepository traceLinkRepo, ArchitectureModelMapper archMapper,
            CodeModelMapper codeMapper, DocumentationPersistenceService documentationService) {
        this.archRepo = archRepo;
        this.codeRepo = codeRepo;
        this.traceLinkRepo = traceLinkRepo;
        this.archMapper = archMapper;
        this.codeMapper = codeMapper;
        this.archModelRepo = archModelRepo;
        this.documentationService = documentationService;
        this.neo4jClient = neo4jClient;
        this.mappingContext = mappingContext;
    }

    @Transactional
    public void deleteTraceLinksByType(TraceLinkType type) {
        traceLinkRepo.deleteLinksByType(type);
    }

    @Transactional
    public void deleteAllTraceLinks() {
        traceLinkRepo.deleteAllTraceLinks();
    }

    public boolean saveTracelinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
        for (TraceLink<?, ?> link : traceLinks) {
            resolveAndSave(link);
        }
        return true; // TODO: Implement proper error handling and return false if any save operation fails
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

    @Transactional(readOnly = true)
    public Set<ArchitectureCodeTraceLink> loadAllArchitectureCodeTraceLinks() {
        return loadLinks(TraceLinkType.ARCHITECTURE_CODE, ArchitectureItemNode.class, (archNode, target) -> {
            if (target instanceof CodeItemNode codeNode) {
                return new ArchitectureCodeTraceLink(archMapper.mapItem(archNode), codeMapper.toDomain(codeNode));
            }
            return null;
        });
    }

    @Transactional(readOnly = true)
    public Set<SentenceModelTraceLink> loadAllSentenceArchitectureModelTraceLinks(String textId) {
        return loadSentenceLinks(textId, TraceLinkType.SENTENCE_ARCHITECTURE, ArchitectureItemNode.class,
                (sEntity, target) -> new SentenceModelTraceLink(sEntity, archMapper.mapItem((ArchitectureItemNode) target)));
    }

    @Transactional(readOnly = true)
    public Set<SentenceModelTraceLink> loadAllSentenceCodeModelTraceLinks(
            String textId) {//TODO: this requires that the preprocessing data is always stored with this ID, which is not ideal. Consider a more flexible approach.
        return loadSentenceLinks(textId, TraceLinkType.SENTENCE_CODE, CodeItemNode.class,
                (sEntity, target) -> new SentenceModelTraceLink(sEntity, codeMapper.toDomain((CodeItemNode) target)));
    }

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
