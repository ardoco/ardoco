/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.RecommendationModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.SentenceModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityOccurrence;
import edu.kit.kastel.mcse.ardoco.core.api.stage.connectiongenerator.ner.NamedArchitectureEntityToModelTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.stage.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.api.text.SentenceEntity;
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
import io.github.ardoco.core.neo4jschema.mapper.DocumentationMapper;
import io.github.ardoco.core.neo4jschema.mapper.NounMappingMapper;
import io.github.ardoco.core.neo4jschema.repository.tracelink.TraceLinkRepository;

@Service
public class TraceLinkPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(TraceLinkPersistenceService.class);

    private static final String TRANSITIVE_QUERY = """
            MATCH (s:Sentence)-[r1:TRACES_TO]->(mid:Traceable)-[r2:TRACES_TO]->(end:Traceable)
            WHERE r1.traceLinkType = $type1 AND r2.traceLinkType = $type2
            RETURN DISTINCT s, mid, end
            """;

    private static final int WRITE_CHUNK_SIZE = 2000;

    private static final String TRACEABLE_SOURCE_QUERY = """
            UNWIND $rows AS row
            OPTIONAL MATCH (s:Traceable {ardocoId: row.sourceId})
            OPTIONAL MATCH (t:Traceable {ardocoId: row.targetId})
            FOREACH (ignore IN CASE WHEN s IS NOT NULL AND t IS NOT NULL THEN [1] ELSE [] END |
                MERGE (s)-[r:TRACES_TO {traceLinkType: row.type}]->(t)
                SET r.confidence = COALESCE(row.confidence, -1.0),
                    r.traceLinkType = row.type)
            WITH row, s, t
            WHERE s IS NULL OR t IS NULL
            RETURN row.sourceId AS sourceId, row.targetId AS targetId, row.type AS type, s IS NULL AS sourceMissing, t IS NULL AS targetMissing
            """;

    private static final String RECOMMENDATION_SOURCE_QUERY = """
            UNWIND $rows AS row
            OPTIONAL MATCH (s:RecommendedInstance {ardocoId: row.sourceId})
            OPTIONAL MATCH (t:Traceable {ardocoId: row.targetId})
            FOREACH (ignore IN CASE WHEN s IS NOT NULL AND t IS NOT NULL THEN [1] ELSE [] END |
                MERGE (s)-[r:TRACES_TO {traceLinkType: row.type}]->(t)
                SET r.confidence = COALESCE(row.confidence, -1.0),
                    r.traceLinkType = row.type)
            WITH row, s, t
            WHERE s IS NULL OR t IS NULL
            RETURN row.sourceId AS sourceId, row.targetId AS targetId, row.type AS type, s IS NULL AS sourceMissing, t IS NULL AS targetMissing
            """;

    private final TraceLinkRepository traceLinkRepo;

    private final Neo4jClient neo4jClient;
    private final Neo4jMappingContext mappingContext;

    private final ArchitectureModelMapper archMapper;
    private final CodeModelMapper codeMapper;
    private final DocumentationMapper documentationMapper;
    public TraceLinkPersistenceService(Neo4jClient neo4jClient, Neo4jMappingContext mappingContext, TraceLinkRepository traceLinkRepo,
            ArchitectureModelMapper archMapper, CodeModelMapper codeMapper, DocumentationMapper documentationMapper) {
        this.traceLinkRepo = traceLinkRepo;
        this.archMapper = archMapper;
        this.codeMapper = codeMapper;
        this.neo4jClient = neo4jClient;
        this.mappingContext = mappingContext;
        this.documentationMapper = documentationMapper;
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
     * Saves a collection of trace links to the database. It assumes that the elements between which the links exist are already present in the database. Each
     * trace link is resolved into one row per relationship (a transitive link into its two legs), with its TraceLinkType determined from the link's class and
     * the types of its endpoints. Rows are deduplicated by (source, target, type), the last occurrence winning, and written in batched {@code UNWIND} queries
     * of at most {@value #WRITE_CHUNK_SIZE} rows, one query shape per source label. Rows whose endpoints are not found, links that cannot be mapped to a
     * known type and endpoints without a resolvable id are collected over all chunks and reported as one ERROR after the last chunk.
     *
     * @param traceLinks A collection of trace links to be saved into the database. This collection can contain different types of trace links, such as
     *                   ArchitectureCodeTraceLink, SentenceModelTraceLink or TransitiveTracelink
     * @return true if every trace link was written
     * @throws MissingTraceLinkEndpointException if any row was not written, any link type could not be determined or any endpoint id could not be resolved
     */
    public boolean saveTracelinks(Collection<? extends TraceLink<?, ?>> traceLinks) {
        Map<RowKey, TraceLinkRow> rows = new LinkedHashMap<>();
        TraceLinkWriteErrors errors = new TraceLinkWriteErrors();
        int rowsBeforeDedup = 0;
        int skippedLinks = 0;
        for (TraceLink<?, ?> link : traceLinks) {
            if (link instanceof TransitiveTraceLink<?, ?> transitive) {
                putRow(rows, transitive.getFirstTraceLink(), TraceLinkType.SENTENCE_ARCHITECTURE, errors);
                putRow(rows, transitive.getSecondTraceLink(), TraceLinkType.ARCHITECTURE_CODE, errors);
                rowsBeforeDedup += 2;
                continue;
            }
            TraceLinkType type = determineType(link);
            if (type == null) {
                errors.recordUnknownType(link.getClass().getName());
                skippedLinks++;
                continue;
            }
            putRow(rows, link, type, errors);
            rowsBeforeDedup++;
        }

        List<TraceLinkRow> traceableSourceRows = new ArrayList<>();
        List<TraceLinkRow> recommendationSourceRows = new ArrayList<>();
        for (TraceLinkRow row : rows.values()) {
            if (isRecommendationType(row.type())) {
                recommendationSourceRows.add(row);
            } else {
                traceableSourceRows.add(row);
            }
        }

        int queries = writeRows(traceableSourceRows, TRACEABLE_SOURCE_QUERY, errors);
        queries += writeRows(recommendationSourceRows, RECOMMENDATION_SOURCE_QUERY, errors);
        logger.debug("saveTracelinks: {} trace links, {} rows before dedup, {} after dedup, {} UNWIND queries, {} rows not written, {} links skipped",
                traceLinks.size(), rowsBeforeDedup, rows.size(), queries, errors.rowsNotWritten(), skippedLinks);
        if (errors.hasErrors()) {
            String message = errors.message();
            logger.error(message);
            throw new MissingTraceLinkEndpointException(message);
        }
        return true;
    }

    private void putRow(Map<RowKey, TraceLinkRow> rows, TraceLink<?, ?> link, TraceLinkType type, TraceLinkWriteErrors errors) {
        String sourceId = getEndpointId(link.getFirstEndpoint(), errors);
        String targetId = getEndpointId(link.getSecondEndpoint(), errors);
        double confidence = -1.0;
        if (isRecommendationType(type) && link instanceof RecommendationModelTraceLink rmtl) {
            confidence = rmtl.getConfidence();
        } else if (type == TraceLinkType.NER_ARCHITECTURE && link instanceof NamedArchitectureEntityToModelTraceLink nerLink) {
            confidence = nerLink.getConfidence();
        }
        rows.put(new RowKey(sourceId, targetId, type), new TraceLinkRow(sourceId, targetId, type, confidence));
    }

    private static boolean isRecommendationType(TraceLinkType type) {
        return type == TraceLinkType.RECOMMENDATION_ARCHITECTURE || type == TraceLinkType.RECOMMENDATION_CODE;
    }

    private int writeRows(List<TraceLinkRow> rows, String query, TraceLinkWriteErrors errors) {
        int queries = 0;
        for (int start = 0; start < rows.size(); start += WRITE_CHUNK_SIZE) {
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (TraceLinkRow row : rows.subList(start, Math.min(start + WRITE_CHUNK_SIZE, rows.size()))) {
                parameters.add(Map.of("sourceId", row.sourceId(), "targetId", row.targetId(), "type", row.type().name(), "confidence", row.confidence()));
            }
            Collection<Map<String, Object>> unresolved = neo4jClient.query(query).bind(parameters).to("rows").fetch().all();
            queries++;
            for (Map<String, Object> result : unresolved) {
                errors.recordMissingEndpoint(String.valueOf(result.get("type")), String.valueOf(result.get("sourceId")), String.valueOf(result.get("targetId")),
                        Boolean.TRUE.equals(result.get("sourceMissing")), Boolean.TRUE.equals(result.get("targetMissing")));
            }
        }
        return queries;
    }

    private TraceLinkType determineType(TraceLink<?, ?> link) {
        if (link instanceof ArchitectureCodeTraceLink)
            return TraceLinkType.ARCHITECTURE_CODE;
        if (link instanceof SentenceModelTraceLink smtl) {
            if (smtl.getSecondEndpoint() instanceof ArchitectureItem) {
                return TraceLinkType.SENTENCE_ARCHITECTURE;
            }
            if (!(smtl.getSecondEndpoint() instanceof CodeItem)) {
                logger.warn("SentenceModelTraceLink second endpoint is neither ArchitectureItem nor CodeItem ({}); defaulting to SENTENCE_CODE",
                        smtl.getSecondEndpoint().getClass().getName());
            }
            return TraceLinkType.SENTENCE_CODE;
        }
        if (link instanceof RecommendationModelTraceLink rmtl) {
            if (rmtl.getSecondEndpoint() instanceof ArchitectureItem) {
                return TraceLinkType.RECOMMENDATION_ARCHITECTURE;
            }
            if (rmtl.getSecondEndpoint() instanceof CodeItem) {
                return TraceLinkType.RECOMMENDATION_CODE;
            }
        }
        if (link instanceof NamedArchitectureEntityToModelTraceLink) {
            return TraceLinkType.NER_ARCHITECTURE;
        }
        return null;
    }

    private String getEndpointId(Object endpoint, TraceLinkWriteErrors errors) {
        if (endpoint instanceof SentenceEntity se) {
            Sentence s = se.getSentence();
            if (s.getWords().isEmpty() && s.getPhrases().isEmpty() && s.getText().equals(String.valueOf(s.getSentenceNumber()))) {
                return "simpletext-sentence-" + s.getSentenceNumber();
            }
            return s.getSentenceNumber() + String.valueOf(s.getText().hashCode());
        }
        if (endpoint instanceof RecommendedInstance ri) {
            return ri.getId();
        }
        if (endpoint instanceof NamedArchitectureEntityOccurrence occ) {
            return occ.getId();
        }
        if (endpoint instanceof ModelEntity me)
            return me.getId();
        String fallbackId = endpoint.toString();
        errors.recordFallbackId(endpoint.getClass().getName(), fallbackId);
        return fallbackId;
    }

    private record RowKey(String sourceId, String targetId, TraceLinkType type) {
    }

    private record TraceLinkRow(String sourceId, String targetId, TraceLinkType type, double confidence) {
    }

    private static final class TraceLinkWriteErrors {
        private static final int MAX_EXAMPLES = 5;

        private int rowsNotWritten;
        private final SortedMap<String, MissingEndpointCounts> missingByType = new TreeMap<>();
        private final List<String> unknownTypeExamples = new ArrayList<>();
        private int unknownTypeLinks;
        private final List<String> fallbackIdExamples = new ArrayList<>();
        private int fallbackIds;

        void recordMissingEndpoint(String type, String sourceId, String targetId, boolean sourceMissing, boolean targetMissing) {
            rowsNotWritten++;
            MissingEndpointCounts counts = missingByType.computeIfAbsent(type, ignored -> new MissingEndpointCounts());
            counts.rows++;
            if (sourceMissing) {
                counts.sourceMissing++;
            }
            if (targetMissing) {
                counts.targetMissing++;
            }
            String side = sourceMissing && targetMissing ? "source and target missing" : sourceMissing ? "source missing" : "target missing";
            addExample(counts.examples, type + ": " + sourceId + " -> " + targetId + " (" + side + ")");
        }

        void recordUnknownType(String linkClass) {
            unknownTypeLinks++;
            addExample(unknownTypeExamples, linkClass);
        }

        void recordFallbackId(String endpointClass, String fallbackId) {
            fallbackIds++;
            addExample(fallbackIdExamples, endpointClass + ": " + fallbackId);
        }

        int rowsNotWritten() {
            return rowsNotWritten;
        }

        boolean hasErrors() {
            return rowsNotWritten > 0 || unknownTypeLinks > 0 || fallbackIds > 0;
        }

        String message() {
            List<String> parts = new ArrayList<>();
            if (rowsNotWritten > 0) {
                List<String> perType = new ArrayList<>();
                List<String> examples = new ArrayList<>();
                missingByType.forEach((type, counts) -> {
                    perType.add(type + ": " + counts.rows + " (source " + counts.sourceMissing + ", target " + counts.targetMissing + ")");
                    examples.addAll(counts.examples);
                });
                parts.add(rowsNotWritten + " trace link(s) not written, endpoint missing - " + String.join(", ", perType) + "; examples: " + examples);
            }
            if (unknownTypeLinks > 0) {
                parts.add(unknownTypeLinks + " trace link(s) not written, type could not be determined - examples: " + unknownTypeExamples);
            }
            if (fallbackIds > 0) {
                parts.add(fallbackIds + " endpoint id(s) fell back to toString() and cannot match a node - examples: " + fallbackIdExamples);
            }
            return "saveTracelinks: " + String.join("; ", parts);
        }

        private static void addExample(List<String> examples, String example) {
            if (examples.size() < MAX_EXAMPLES) {
                examples.add(example);
            }
        }
    }

    private static final class MissingEndpointCounts {
        private int rows;
        private int sourceMissing;
        private int targetMissing;
        private final List<String> examples = new ArrayList<>();
    }

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
    private <S extends TraceableNode> Set<SentenceModelTraceLink> loadSentenceLinks(TraceLinkType type, Class<S> targetClass,
            BiFunction<SentenceEntity, TraceableNode, SentenceModelTraceLink> mapper) {

        return loadLinks(type, SentenceNode.class, (sNode, targetNode) -> {
            if (targetClass.isInstance(targetNode)) {
                Sentence domainSentence = documentationMapper.mapSentenceToDomain(sNode, new HashMap<>());
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
     *         exist, it returns an empty set.
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
     * @return A set of SentenceModelTraceLink objects representing all sentence-architecture trace links currently stored in the database.
     */
    @Transactional(readOnly = true)
    public Set<SentenceModelTraceLink> loadAllSentenceArchitectureModelTraceLinks() {
        return loadSentenceLinks(TraceLinkType.SENTENCE_ARCHITECTURE, ArchitectureItemNode.class, (sEntity, target) -> new SentenceModelTraceLink(sEntity,
                archMapper.mapItem((ArchitectureItemNode) target)));
    }

    /**
     * Loads all SentenceModelTraceLinks of type SENTENCE_CODE from the database for the given text ID. Note: the loaded code items won't have their
     * relationships to other architecture items loaded, as this method is optimized for loading sentence links and not for reconstructing the full architecture
     * model.
     *
     * @return A set of SentenceModelTraceLink objects representing all sentence-code trace links currently stored in the database.
     */
    @Transactional(readOnly = true)
    public Set<SentenceModelTraceLink> loadAllSentenceCodeModelTraceLinks() {
        return loadSentenceLinks(TraceLinkType.SENTENCE_CODE, CodeItemNode.class, (sEntity, target) -> new SentenceModelTraceLink(sEntity, codeMapper.toDomain(
                (CodeItemNode) target)));
    }

    /**
     * Loads RecommendationModelTraceLinks (RI → Architecture or RI → Code). Endpoints are resolved from the provided maps
     * of recommended instances and architecture or code items.
     */
    @Transactional(readOnly = true)
    public List<RecommendationModelTraceLink> loadAllRecommendationArchitectureTraceLinks(SortedMap<String, RecommendedInstance> recommendedInstancesById,
            SortedMap<String, ArchitectureItem> architectureItemsById) {
        return loadRecommendationModelLinks(recommendedInstancesById, architectureItemsById, null, TraceLinkType.RECOMMENDATION_ARCHITECTURE);
    }

    /**
     * Loads RecommendationModelTraceLinks of type {@link TraceLinkType#RECOMMENDATION_CODE}.
     */
    @Transactional(readOnly = true)
    public List<RecommendationModelTraceLink> loadAllRecommendationCodeTraceLinks(SortedMap<String, RecommendedInstance> recommendedInstancesById,
            SortedMap<String, CodeItem> codeItemsById) {
        return loadRecommendationModelLinks(recommendedInstancesById, null, codeItemsById, TraceLinkType.RECOMMENDATION_CODE);
    }

    private List<RecommendationModelTraceLink> loadRecommendationModelLinks(SortedMap<String, RecommendedInstance> recommendedInstancesById,
            SortedMap<String, ArchitectureItem> architectureItemsById, SortedMap<String, CodeItem> codeItemsById, TraceLinkType type) {
        List<RecommendationModelTraceLink> result = new ArrayList<>();
        for (Map<String, Object> row : findRecommendationModelLinkRows(type.name())) {
            String riId = String.valueOf(row.get("riId"));
            String targetId = String.valueOf(row.get("targetId"));
            RecommendedInstance ri = recommendedInstancesById.get(riId);
            ModelEntity target = null;
            if (architectureItemsById != null) {
                target = architectureItemsById.get(targetId);
            }
            if (target == null && codeItemsById != null) {
                target = codeItemsById.get(targetId);
            }
            if (ri == null || target == null) {
                logger.warn("Skipping RI→Model link resume ({}): missing endpoint ri={} target={}", type, riId, targetId);
                continue;
            }
            Object confObj = row.get("confidence");
            double confidence = confObj instanceof Number number ? number.doubleValue() : -1.0;
            if (confidence >= 0) {
                result.add(new RecommendationModelTraceLink(ri, target, NounMappingMapper.RESUME_CLAIMANT, confidence));
            } else {
                result.add(new RecommendationModelTraceLink(ri, target));
            }
        }
        logger.info("Loaded {} RecommendationModelTraceLinks ({}) from Neo4j", result.size(), type);
        return result;
    }

    private Collection<Map<String, Object>> findRecommendationModelLinkRows(String traceLinkType) {
        return neo4jClient.query("""
                MATCH (ri:RecommendedInstance)-[r:TRACES_TO]->(t:Traceable)
                WHERE r.traceLinkType = $type
                RETURN ri.ardocoId AS riId, t.ardocoId AS targetId, coalesce(r.confidence, -1.0) AS confidence
                """)
                .bind(traceLinkType)
                .to("type")
                .fetch()
                .all();
    }

    /**
     * Loads all transitive trace links from sentences over architecture items to code items from the database for the given text ID. This method executes a
     * custom Cypher query to retrieve all chains of trace links that connect sentences to architecture items and then to code items.
     *
     * @return A set of TransitiveTraceLink objects representing all transitive trace links from sentences to architecture items to code items currently stored
     *         in the database. If no such links exist, it returns an empty set.
     */
    @Transactional(readOnly = true)
    public Set<TraceLink<SentenceEntity, ? extends ModelEntity>> loadTransitiveTraceLinks() {
        var sentenceMapper = mappingContext.getRequiredMappingFunctionFor(SentenceNode.class);
        var traceableMapper = mappingContext.getRequiredMappingFunctionFor(TraceableNode.class);

        return neo4jClient.query(TRANSITIVE_QUERY)
                .bind(TraceLinkType.SENTENCE_ARCHITECTURE.name())
                .to("type1")
                .bind(TraceLinkType.ARCHITECTURE_CODE.name())
                .to("type2")
                .fetchAs(TransitiveChainQueryResult.class)
                .mappedBy((typeSystem, record) -> new TransitiveChainQueryResult(sentenceMapper.apply(typeSystem, record.get("s")), traceableMapper.apply(
                        typeSystem, record.get("mid")), traceableMapper.apply(typeSystem, record.get("end"))))
                .all()
                .stream()
                .map(this::createTransitiveLink)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    private Optional<TransitiveTraceLink<SentenceEntity, ? extends ModelEntity>> createTransitiveLink(TransitiveChainQueryResult chain) {
        Sentence domainSentence = documentationMapper.mapSentenceToDomain(chain.getSentence(), new HashMap<>());

        SentenceEntity sentenceEntity = new SentenceEntity(domainSentence);
        ArchitectureItem archMid = mapToArchitectureItem(chain.getArchitecture());
        CodeItem codeEnd = mapToCodeItem(chain.getCode());

        var transitiveLink = TransitiveTraceLink.createTransitiveTraceLink(new SentenceModelTraceLink(sentenceEntity, archMid), new ArchitectureCodeTraceLink(
                archMid, codeEnd));

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
