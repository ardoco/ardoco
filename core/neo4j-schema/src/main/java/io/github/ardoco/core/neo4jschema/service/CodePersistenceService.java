/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import io.github.ardoco.core.neo4jschema.entities.codeModel.ClassUnitNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeAssemblyNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeCompilationUnitNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeItemNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeModelNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeModuleNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodePackageNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.ControlElementNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.DatatypeNode;
import io.github.ardoco.core.neo4jschema.entities.codeModel.InterfaceUnitNode;
import io.github.ardoco.core.neo4jschema.mapper.CodeModelMapper;
import io.github.ardoco.core.neo4jschema.repository.codeModel.CodeModelRepository;

@Service
public class CodePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(CodePersistenceService.class);

    private final CodeModelRepository repository;
    private final CodeModelMapper mapper;
    private final Neo4jClient neo4jClient;

    public CodePersistenceService(CodeModelRepository repository, CodeModelMapper mapper, Neo4jClient neo4jClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.neo4jClient = neo4jClient;
    }

    /**
     * Deletes a specific code model by its metamodel type. Note: This assumes one model per metamodel type.
     *
     * @param metamodel The metamodel type of the code model(s) to be deleted from the database.
     */
    @Transactional
    public void deleteCodeModel(Metamodel metamodel) {
        repository.deleteByMetamodel(metamodel.name());
    }

    /**
     * Retrieves a sorted set of all metamodel types for which code models are currently stored in the database. If any unknown metamodel types are encountered
     * (i.e., those that cannot be mapped to the Metamodel enum), a warning is logged, and those entries are skipped.
     *
     * @return A SortedSet of Metamodel enum values representing the types of code models available in the database.
     */
    @Transactional(readOnly = true)
    public SortedSet<Metamodel> getStoredCodeModelMetamodels() {
        SortedSet<Metamodel> available = new java.util.TreeSet<>();
        List<CodeModelNode> archNodes = repository.findAll();
        for (CodeModelNode node : archNodes) {
            available.add(Metamodel.valueOf(node.getMetamodel()));
        }
        return available;
    }

    /**
     * Loads a code model from the database based on the provided metamodel type. If multiple models exist for the same metamodel, it returns the first one
     * found. If no model is found for the given metamodel, it returns null.
     *
     * @param metamodel The metamodel type used to locate the CodeModelNode in the database.
     * @return The fist CodeModel found for the given metamodel type, or null if no such model exists.
     */
    @Transactional(readOnly = true)
    public Optional<CodeModel> loadCodeModel(Metamodel metamodel) {
        // Spring Data Neo4j's default findAll() does not reliably hydrate the deeply self-referential CONTAINS_CODE_ITEM tree: for large code models the nested
        // children are silently truncated (only the shallow part of the graph is materialized). Instead of relying on SDN's depth-limited traversal, we load every
        // item that belongs to the model (via HAS_REPOSITORY_ITEM) as a flat node and rebuild all associations (content order, parent/compilation-unit/parent-
        // datatype ids, datatype-reference lists) from the verbatim node properties, then reuse CodeModelMapper#toDomain(CodeModelNode). Because
        // CodeItemNode#ardocoId is the @Id, nodes are rewired purely by id. The CONTAINS_CODE_ITEM / EXTENDS / ... relationships still exist in the graph for
        // querying, but they are not needed here because the ordered/nullable state lives in the properties.
        List<String> modelIds = new ArrayList<>(neo4jClient.query("MATCH (m:CodeModel {metamodel: $mt}) RETURN m.modelId AS modelId ORDER BY modelId")
                .bind(metamodel.name())
                .to("mt")
                .fetchAs(String.class)
                .mappedBy((typeSystem, record) -> record.get("modelId").asString(null))
                .all());
        modelIds.removeIf(java.util.Objects::isNull);
        if (modelIds.isEmpty()) {
            return Optional.empty();
        }
        String modelId = modelIds.get(0);
        if (modelIds.size() > 1) {
            logger.warn("{} code models stored for metamodel {} ({}); loading only {}", modelIds.size(), metamodel, modelIds, modelId);
        }

        // 1) Every code item that belongs to the model (via HAS_REPOSITORY_ITEM), independent of hierarchy reachability.
        Collection<Map<String, Object>> itemRows = neo4jClient.query(
                """
                        MATCH (m:CodeModel {modelId: $modelId})-[:HAS_REPOSITORY_ITEM]->(n:CodeItem)
                        RETURN n.ardocoId AS id, labels(n) AS labels, properties(n) AS props
                        """)
                .bind(modelId)
                .to("modelId")
                .fetch()
                .all();

        Map<String, CodeItemNode> nodeMap = new HashMap<>();
        for (Map<String, Object> row : itemRows) {
            String id = (String) row.get("id");
            @SuppressWarnings("unchecked")
            List<String> labels = (List<String>) row.get("labels");
            @SuppressWarnings("unchecked")
            Map<String, Object> props = (Map<String, Object>) row.get("props");
            nodeMap.put(id, instantiateItemNode(id, labels, props));
        }

        // 2) Roots of the model.
        Collection<Map<String, Object>> rootRows = neo4jClient.query(
                "MATCH (m:CodeModel {modelId: $modelId})-[:CONTAINS_CODE_ROOT]->(root:CodeItem) RETURN root.ardocoId AS rootId")
                .bind(modelId)
                .to("modelId")
                .fetch()
                .all();

        CodeModelNode modelNode = new CodeModelNode(modelId, metamodel.name());
        for (Map<String, Object> row : rootRows) {
            CodeItemNode root = nodeMap.get((String) row.get("rootId"));
            if (root != null) {
                modelNode.addContent(root);
            }
        }
        // Register the full item set so CodeModelMapper#toDomain can restore items that are not reachable from a root via the containment hierarchy.
        for (CodeItemNode itemNode : nodeMap.values()) {
            modelNode.addRepositoryItem(itemNode);
        }

        return Optional.of(mapper.toDomain(modelNode));
    }

    /**
     * Reconstructs a concrete {@link CodeItemNode} subtype from its stored Neo4j labels and properties. The most specific label wins so that, e.g., a
     * {@code ClassUnit} (which also carries the {@code Datatype} and {@code CodeItem} labels) is instantiated as a {@link ClassUnitNode}.
     */
    private CodeItemNode instantiateItemNode(String id, List<String> labels, Map<String, Object> props) {
        String name = (String) props.get("name");
        CodeItemNode node;
        if (labels.contains("CodePackage")) {
            node = new CodePackageNode(name, id);
        } else if (labels.contains("CodeCompilationUnit")) {
            node = new CodeCompilationUnitNode(name, id, (String) props.get("extension"), (String) props.get("language"), toStringList(props.get("pathElements")));
        } else if (labels.contains("CodeAssembly")) {
            node = new CodeAssemblyNode(name, id, (String) props.get("language"));
        } else if (labels.contains("ClassUnit")) {
            node = new ClassUnitNode(name, id);
        } else if (labels.contains("InterfaceUnit")) {
            node = new InterfaceUnitNode(name, id);
        } else if (labels.contains("ControlElement")) {
            node = new ControlElementNode(name, id);
        } else if (labels.contains("CodeModule")) {
            node = new CodeModuleNode(name, id);
        } else {
            throw new IllegalStateException("Unknown code item labels for node " + id + ": " + labels);
        }

        // Restore the explicitly persisted parent id and verbatim content order (see CodeItemNode#getParentModuleId() / #getContentIds()).
        node.setParentModuleId((String) props.get("parentModuleId"));
        // Preserve null (property absent) vs empty list: toStringList() would collapse a missing property into [], which then overwrites a null domain content list.
        node.setContentIds(toStringListOrNull(props.get("contentIds")));
        if (node instanceof DatatypeNode datatypeNode) {
            datatypeNode.setCompilationUnitId((String) props.get("compilationUnitId"));
            datatypeNode.setParentDatatypeId((String) props.get("parentDatatypeId"));
            // A property that was null/empty on save is absent here, so these come back null - which is exactly what the domain needs to distinguish a null id list
            // from an empty one.
            datatypeNode.setExtendedDataTypesIds(toStringListOrNull(props.get("extendedDataTypesIds")));
            datatypeNode.setImplementedDataTypesIds(toStringListOrNull(props.get("implementedDataTypesIds")));
            datatypeNode.setDatatypeReferencesIds(toStringListOrNull(props.get("datatypeReferencesIds")));
        }
        return node;
    }

    private static List<String> toStringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value == null) {
            return result;
        }
        if (value instanceof List<?> list) {
            for (Object element : list) {
                result.add(element == null ? null : element.toString());
            }
            return result;
        }
        // Neo4j sometimes surfaces single-element collections as a bare value, or arrays as Object[].
        if (value instanceof Object[] array) {
            for (Object element : array) {
                result.add(element == null ? null : element.toString());
            }
            return result;
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object element : iterable) {
                result.add(element == null ? null : element.toString());
            }
            return result;
        }
        result.add(value.toString());
        return result;
    }

    /**
     * Like {@link #toStringList(Object)} but preserves {@code null}: a missing/{@code null} property comes back as {@code null} (not an empty list). This lets the
     * domain distinguish a {@code null} datatype-reference list from an empty one, which it treats as unequal.
     */
    private static List<String> toStringListOrNull(Object value) {
        return value == null ? null : toStringList(value);
    }

    /**
     * Saves the given code model. This method first deletes any existing model with the same ID to ensure that stale data is not retained.
     * <p>
     * Deliberately does not use {@code repository.save(CodeModelNode)}: a plain Spring Data cascade walks the whole cyclic
     * {@code CodeItemNode} object graph with its own per-node id resolution (the code model is the single largest contributor to base-persistence
     * time - TeaStore: tens of seconds for ~3,800 items). {@link CodeModelMapper#toNode} already builds the complete, de-duplicated node graph (one
     * {@link CodeItemNode} per domain item, cached by id); this method only replaces the final persistence step with targeted, batched Cypher
     * ({@code UNWIND}/{@code CREATE}), grouped by concrete item type since Cypher cannot parameterize a node label without APOC.
     *
     * @param model The domain CodeModel object to be saved into the database.
     */
    @Transactional
    public void saveCodeModel(CodeModel model) {
        repository.deleteByModelId(model.getId());
        CodeModelNode modelNode = mapper.toNode(model);
        persistCodeModelBatched(modelNode);
    }

    private static final int BATCH_CHUNK_SIZE = 2000;

    private void persistCodeModelBatched(CodeModelNode modelNode) {
        neo4jClient.query("CREATE (m:CodeModel {id: $id, modelId: $modelId, metamodel: $metamodel})")
                .bind(UUID.randomUUID().toString())
                .to("id")
                .bind(modelNode.getModelId())
                .to("modelId")
                .bind(modelNode.getMetamodel())
                .to("metamodel")
                .run();

        Collection<CodeItemNode> items = modelNode.getAllRepositoryItems();
        createCodeItemNodes(items);
        createContainsCodeItemEdges(items);
        createHasRepositoryItemEdges(modelNode.getModelId(), items);
        createContainsCodeRootEdges(modelNode.getModelId(), modelNode.getContent());
        createDatatypeReferenceEdges(items);
    }

    /**
     * Groups {@code items} by concrete node type (Cypher cannot parameterize a label without APOC) and creates each group in one batched,
     * chunked {@code UNWIND}/{@code CREATE} per type. Every concrete {@link CodeItemNode} subtype is handled, even ones with zero instances in a
     * given model, so the full label/property shape is always available.
     */
    private void createCodeItemNodes(Collection<CodeItemNode> items) {
        Map<String, Map<String, Object>> codePackages = new LinkedHashMap<>();
        Map<String, Map<String, Object>> codeModules = new LinkedHashMap<>();
        Map<String, Map<String, Object>> codeAssemblies = new LinkedHashMap<>();
        Map<String, Map<String, Object>> controlElements = new LinkedHashMap<>();
        Map<String, Map<String, Object>> compilationUnits = new LinkedHashMap<>();
        Map<String, Map<String, Object>> classUnits = new LinkedHashMap<>();
        Map<String, Map<String, Object>> interfaceUnits = new LinkedHashMap<>();

        for (CodeItemNode node : items) {
            Map<String, Object> props = baseItemProps(node);
            if (node instanceof CodeCompilationUnitNode c) {
                putIfNotNull(props, "extension", c.getExtension());
                putIfNotNull(props, "language", c.getLanguage());
                putIfNotNull(props, "pathElements", c.getPathElements());
                compilationUnits.put(node.getArdocoId(), props);
            } else if (node instanceof CodeAssemblyNode c) {
                putIfNotNull(props, "language", c.getLanguage());
                codeAssemblies.put(node.getArdocoId(), props);
            } else if (node instanceof ClassUnitNode c) {
                addDatatypeProps(props, c);
                classUnits.put(node.getArdocoId(), props);
            } else if (node instanceof InterfaceUnitNode c) {
                addDatatypeProps(props, c);
                interfaceUnits.put(node.getArdocoId(), props);
            } else if (node instanceof CodeModuleNode) {
                codeModules.put(node.getArdocoId(), props);
            } else if (node instanceof CodePackageNode) {
                codePackages.put(node.getArdocoId(), props);
            } else if (node instanceof ControlElementNode) {
                controlElements.put(node.getArdocoId(), props);
            } else {
                throw new IllegalStateException("Unknown CodeItemNode subtype: " + node.getClass().getName());
            }
        }

        createNodesByLabel("CodePackage:CodeItem:Traceable", codePackages.values());
        createNodesByLabel("CodeModule:CodeItem:Traceable", codeModules.values());
        createNodesByLabel("CodeAssembly:CodeModule:CodeItem:Traceable", codeAssemblies.values());
        createNodesByLabel("ControlElement:CodeItem:Traceable", controlElements.values());
        createNodesByLabel("CodeCompilationUnit:CodeItem:Traceable", compilationUnits.values());
        createNodesByLabel("ClassUnit:Datatype:CodeItem:Traceable", classUnits.values());
        createNodesByLabel("InterfaceUnit:Datatype:CodeItem:Traceable", interfaceUnits.values());
    }

    private Map<String, Object> baseItemProps(CodeItemNode node) {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("ardocoId", node.getArdocoId());
        putIfNotNull(props, "name", node.getName());
        putIfNotNull(props, "parentModuleId", node.getParentModuleId());
        putIfNotNull(props, "contentIds", node.getContentIds());
        return props;
    }

    private void addDatatypeProps(Map<String, Object> props, DatatypeNode node) {
        putIfNotNull(props, "compilationUnitId", node.getCompilationUnitId());
        putIfNotNull(props, "parentDatatypeId", node.getParentDatatypeId());
        putIfNotNull(props, "extendedDataTypesIds", node.getExtendedDataTypesIds());
        putIfNotNull(props, "implementedDataTypesIds", node.getImplementedDataTypesIds());
        putIfNotNull(props, "datatypeReferencesIds", node.getDatatypeReferencesIds());
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private void createNodesByLabel(String labels, Collection<Map<String, Object>> items) {
        if (items.isEmpty()) {
            return;
        }
        for (List<Map<String, Object>> chunk : chunk(new ArrayList<>(items), BATCH_CHUNK_SIZE)) {
            neo4jClient.query("""
                    UNWIND $items AS item
                    CREATE (n:%s)
                    SET n += item
                    """.formatted(labels))
                    .bind(chunk)
                    .to("items")
                    .run();
        }
    }

    /** {@code CONTAINS_CODE_ITEM}: only for children actually resolvable in the model (mirrors the in-memory node graph, not the raw {@code contentIds}). */
    private void createContainsCodeItemEdges(Collection<CodeItemNode> items) {
        List<Map<String, Object>> edges = new ArrayList<>();
        for (CodeItemNode parent : items) {
            for (CodeItemNode child : parent.getContent()) {
                edges.add(Map.of("parentId", parent.getArdocoId(), "childId", child.getArdocoId()));
            }
        }
        createTraceableToTraceableEdges("CONTAINS_CODE_ITEM", edges, "parentId", "childId", "CONTAINS_CODE_ITEM target(s) not found");
    }

    private void createHasRepositoryItemEdges(String modelId, Collection<CodeItemNode> items) {
        List<String> itemIds = items.stream().map(CodeItemNode::getArdocoId).toList();
        for (List<String> idChunk : chunk(itemIds, BATCH_CHUNK_SIZE)) {
            Long missing = neo4jClient.query("""
                    MATCH (m:CodeModel {modelId: $modelId})
                    UNWIND $itemIds AS itemId
                    OPTIONAL MATCH (n:Traceable {ardocoId: itemId})
                    WITH m, itemId, n
                    FOREACH (ignore IN CASE WHEN n IS NOT NULL THEN [1] ELSE [] END | CREATE (m)-[:HAS_REPOSITORY_ITEM]->(n))
                    RETURN sum(CASE WHEN n IS NULL THEN 1 ELSE 0 END) AS missing
                    """)
                    .bind(modelId)
                    .to("modelId")
                    .bind(idChunk)
                    .to("itemIds")
                    .fetchAs(Long.class)
                    .one()
                    .orElse(0L);
            if (missing != null && missing > 0) {
                logger.warn("HAS_REPOSITORY_ITEM: {} item(s) not found while linking to CodeModel {}", missing, modelId);
            }
        }
    }

    private void createContainsCodeRootEdges(String modelId, Collection<CodeItemNode> roots) {
        if (roots.isEmpty()) {
            return;
        }
        List<String> rootIds = roots.stream().map(CodeItemNode::getArdocoId).toList();
        for (List<String> idChunk : chunk(rootIds, BATCH_CHUNK_SIZE)) {
            Long missing = neo4jClient.query("""
                    MATCH (m:CodeModel {modelId: $modelId})
                    UNWIND $rootIds AS rootId
                    OPTIONAL MATCH (n:Traceable {ardocoId: rootId})
                    WITH m, rootId, n
                    FOREACH (ignore IN CASE WHEN n IS NOT NULL THEN [1] ELSE [] END | CREATE (m)-[:CONTAINS_CODE_ROOT]->(n))
                    RETURN sum(CASE WHEN n IS NULL THEN 1 ELSE 0 END) AS missing
                    """)
                    .bind(modelId)
                    .to("modelId")
                    .bind(idChunk)
                    .to("rootIds")
                    .fetchAs(Long.class)
                    .one()
                    .orElse(0L);
            if (missing != null && missing > 0) {
                logger.warn("CONTAINS_CODE_ROOT: {} root(s) not found while linking to CodeModel {}", missing, modelId);
            }
        }
    }

    /** {@code EXTENDS}/{@code IMPLEMENTS}/{@code REFERENCES_DATATYPE}, from the verbatim id-list properties (not the {@code Set<DatatypeNode>} fields). */
    private void createDatatypeReferenceEdges(Collection<CodeItemNode> items) {
        List<Map<String, Object>> extendsEdges = new ArrayList<>();
        List<Map<String, Object>> implementsEdges = new ArrayList<>();
        List<Map<String, Object>> referencesEdges = new ArrayList<>();
        for (CodeItemNode node : items) {
            if (!(node instanceof DatatypeNode dt)) {
                continue;
            }
            addEdges(extendsEdges, dt.getArdocoId(), dt.getExtendedDataTypesIds());
            addEdges(implementsEdges, dt.getArdocoId(), dt.getImplementedDataTypesIds());
            addEdges(referencesEdges, dt.getArdocoId(), dt.getDatatypeReferencesIds());
        }
        createTraceableToTraceableEdges("EXTENDS", extendsEdges, "sourceId", "targetId", "EXTENDS target(s) not found");
        createTraceableToTraceableEdges("IMPLEMENTS", implementsEdges, "sourceId", "targetId", "IMPLEMENTS target(s) not found");
        createTraceableToTraceableEdges("REFERENCES_DATATYPE", referencesEdges, "sourceId", "targetId", "REFERENCES_DATATYPE target(s) not found");
    }

    private static void addEdges(List<Map<String, Object>> edges, String sourceId, List<String> targetIds) {
        if (targetIds == null) {
            return;
        }
        for (String targetId : targetIds) {
            edges.add(Map.of("sourceId", sourceId, "targetId", targetId));
        }
    }

    private void createTraceableToTraceableEdges(String relationshipType, List<Map<String, Object>> edges, String sourceKey, String targetKey,
            String missingWarning) {
        if (edges.isEmpty()) {
            return;
        }
        for (List<Map<String, Object>> chunk : chunk(edges, BATCH_CHUNK_SIZE)) {
            Long missing = neo4jClient.query("""
                    UNWIND $edges AS edge
                    OPTIONAL MATCH (s:Traceable {ardocoId: edge.%s})
                    OPTIONAL MATCH (t:Traceable {ardocoId: edge.%s})
                    WITH s, t
                    FOREACH (ignore IN CASE WHEN s IS NOT NULL AND t IS NOT NULL THEN [1] ELSE [] END | CREATE (s)-[:%s]->(t))
                    RETURN sum(CASE WHEN s IS NULL OR t IS NULL THEN 1 ELSE 0 END) AS missing
                    """.formatted(sourceKey, targetKey, relationshipType))
                    .bind(chunk)
                    .to("edges")
                    .fetchAs(Long.class)
                    .one()
                    .orElse(0L);
            if (missing != null && missing > 0) {
                logger.warn("{}: {}", missingWarning, missing);
            }
        }
    }

    private static <T> List<List<T>> chunk(List<T> items, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < items.size(); i += size) {
            chunks.add(items.subList(i, Math.min(i + size, items.size())));
        }
        return chunks;
    }

}
