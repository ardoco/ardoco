/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.entity.ModelEntity;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TransitiveTraceLink;
import io.github.ardoco.core.neo4jschema.entities.documentation.SentenceNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceableNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TransitiveChainQueryResult;
import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureModelRepository;

import io.github.ardoco.core.neo4jschema.service.documentation.DocumentationPersistenceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.data.neo4j.core.mapping.Neo4jMappingContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.stage.codetraceability.ArchitectureCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeItemNode;
import io.github.ardoco.core.neo4jschema.entities.tracelink.TraceLinkType;
import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureItemRepository;
import io.github.ardoco.core.neo4jschema.repository.codeModel.CodeItemRepository;
import io.github.ardoco.core.neo4jschema.repository.tracelink.TraceLinkRepository;
import io.github.ardoco.core.neo4jschema.service.architectureModel.ArchitectureModelMapper;
import io.github.ardoco.core.neo4jschema.service.codeModel.CodeModelMapper;

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

    public TraceLinkPersistenceService(Neo4jClient neo4jClient, Neo4jMappingContext mappingContext, ArchitectureItemRepository archRepo, ArchitectureModelRepository archModelRepo, CodeItemRepository codeRepo, TraceLinkRepository traceLinkRepo,
            ArchitectureModelMapper archMapper, CodeModelMapper codeMapper, DocumentationPersistenceService documentationService) {
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

    public boolean saveTracelinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
        for (TraceLink<?, ?> link : traceLinks) {
            if (link instanceof ArchitectureCodeTraceLink archCodeLink) { // is of type TraceLink<ArchitectureItem, CodeItem>
                saveAtomicLink(archCodeLink, TraceLinkType.ARCHITECTURE_CODE);

            } else if (link instanceof TransitiveTraceLink<?,?> transitive) { // is of type TraceLink<SentenceEntity, ? extends ModelEntity>
                saveAtomicLink(transitive.getFirstTraceLink(), TraceLinkType.SENTENCE_ARCHITECTURE);
                saveAtomicLink(transitive.getSecondTraceLink(), TraceLinkType.ARCHITECTURE_CODE);

                // Storing the transitive link explicitly is not really needed since we can always reconstruct the transitive link from the atomic links, this is only for completeness

            } else if (link instanceof SentenceModelTraceLink sentenceArchLink) { // is of type TraceLink<SentenceEntity, ArchitectureItem>
                saveAtomicLink(sentenceArchLink, TraceLinkType.SENTENCE_ARCHITECTURE);
            }
        }
        return true; // TODO: Implement proper error handling and return false if any save operation fails
    }

    public boolean saveTransitiveTracelinks(Collection<? extends TransitiveTraceLink<?,?>> traceLinks) {
        Collection<TraceLink<?, ?>> firstLinks = traceLinks.stream()
                .map(TransitiveTraceLink::getFirstTraceLink)
                .collect(Collectors.toSet());
        Collection<TraceLink<?, ?>> secondLinks = traceLinks.stream()
                .map(TransitiveTraceLink::getSecondTraceLink)
                .collect(Collectors.toSet());

        logger.info("Saving {} first links and {} second links for transitive trace links", firstLinks.size(), secondLinks.size());

        saveTracelinks(firstLinks);
        saveTracelinks(secondLinks);
        return true;
    }

    private String getArdocoIdForSentence(Sentence sentence) {
        return String.valueOf(sentence.getSentenceNumber()) + sentence.getText().hashCode();
    }

    private void saveAtomicLink(TraceLink<?, ?> link, TraceLinkType type) {
        var source = link.getFirstEndpoint();
        var target = link.getSecondEndpoint();

        if (source instanceof SentenceEntity sentence) {
            traceLinkRepo.createTraceLink(
                    getArdocoIdForSentence(sentence.getSentence()),
                    target.getId(),
                    type
            );
        } else {
            traceLinkRepo.createTraceLink(
                    source.getId(),
                    target.getId(),
                    type
            );
        }
    }


    @Transactional(readOnly = true)
    public Set<ArchitectureCodeTraceLink> loadAllArchitectureCodeTraceLinks() {
        return traceLinkRepo.findAllByRelationshipType(TraceLinkType.ARCHITECTURE_CODE).stream()
                .filter(ArchitectureItemNode.class::isInstance)
                .map(ArchitectureItemNode.class::cast)
                .flatMap(archNode -> archNode.getOutgoingLinks().stream()
                        .filter(rel -> rel.getTraceLinkType() == TraceLinkType.ARCHITECTURE_CODE)
                        .filter(rel -> rel.getTargetNode() instanceof CodeItemNode)
                        .map(rel -> new ArchitectureCodeTraceLink(
                                archMapper.mapItem(archNode),
                                codeMapper.mapItem((CodeItemNode) rel.getTargetNode())
                        ))
                )
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<SentenceModelTraceLink> loadAllSentenceModelTraceLinks() {
        Text domainText = documentationService.loadPreprocessedText();
        return traceLinkRepo.findAllByRelationshipType(TraceLinkType.SENTENCE_ARCHITECTURE).stream()
                .filter(SentenceNode.class::isInstance)
                .map(SentenceNode.class::cast)
                .flatMap(sentenceNode -> sentenceNode.getOutgoingLinks().stream()
                        .filter(rel -> rel.getTraceLinkType() == TraceLinkType.SENTENCE_ARCHITECTURE)
                        .filter(rel -> rel.getTargetNode() instanceof ArchitectureItemNode)
                        .map(rel -> new SentenceModelTraceLink(
                                new SentenceEntity(castSentenceNodeToEntity(sentenceNode, domainText)),
                                archMapper.mapItem((ArchitectureItemNode) rel.getTargetNode())
                        ))
                )
                .collect(Collectors.toSet());
    }

    private Sentence castSentenceNodeToEntity(SentenceNode sentenceNode, Text domainText) {
        Sentence sentence = domainText.getSentences()
                .detect(s -> s.getSentenceNumber() == sentenceNode.getSentenceNumber());
        return sentence;
    }

    @Transactional(readOnly = true)
    public Set<TraceLink<SentenceEntity, ? extends ModelEntity>> loadTransitiveTraceLinks() {

        Text domainText = documentationService.loadPreprocessedText();
        if (domainText == null) {
            logger.warn("No preprocessed text available, cannot load transitive trace links.");
            return new HashSet<>();
        }

        var sentenceMapper = mappingContext.getRequiredMappingFunctionFor(SentenceNode.class);
        var traceableMapper = mappingContext.getRequiredMappingFunctionFor(TraceableNode.class);

        Collection<TransitiveChainQueryResult> chains = neo4jClient.query(
                        "MATCH (s:Sentence)-[r1:TRACES_TO]->(mid:Traceable)-[r2:TRACES_TO]->(end:Traceable) " +
                                "WHERE r1.traceLinkType = $type1 AND r2.traceLinkType = $type2 " +
                                "RETURN DISTINCT s, mid, end"
                )
                .bind(TraceLinkType.SENTENCE_ARCHITECTURE.name()).to("type1")
                .bind(TraceLinkType.ARCHITECTURE_CODE.name()).to("type2")
                .fetchAs(TransitiveChainQueryResult.class)
                .mappedBy((typeSystem, record) -> {
                    SentenceNode s = sentenceMapper.apply(typeSystem, record.get("s"));
                    TraceableNode mid = traceableMapper.apply(typeSystem, record.get("mid"));
                    TraceableNode end = traceableMapper.apply(typeSystem, record.get("end"));
                    return new TransitiveChainQueryResult(s, mid, end);
                })
                .all();



        return chains.stream()
                .map(chain -> {
                    Sentence domainSentence = domainText.getSentences()
                            .detect(s -> s.getSentenceNumber() == chain.getSentence().getSentenceNumber());

                    if (domainSentence == null) return Optional.<TransitiveTraceLink<SentenceEntity, ? extends ModelEntity>>empty();

                    SentenceEntity sentenceEntity = new SentenceEntity(domainSentence);

                    // Use your existing helper methods (Polymorphism/instanceof works here!)
                    ArchitectureItem archMid = mapToArchitectureItem(chain.getArchitecture());
                    CodeItem codeEnd = mapToCodeItem(chain.getCode());

                    SentenceModelTraceLink link1 = new SentenceModelTraceLink(sentenceEntity, archMid);
                    ArchitectureCodeTraceLink link2 = new ArchitectureCodeTraceLink(archMid, codeEnd);

                    return TransitiveTraceLink.createTransitiveTraceLink(link1, link2);
                })
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
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
            return codeMapper.mapItem(codeNode);
        }
        throw new IllegalStateException("Unexpected node type in transitive chain: " + node.getClass() + ", expected CodeItemNode");
    }
}
