/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.IdentityHashMap;
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

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureComponentNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureInterfaceNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureMethodNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureModelNode;
import io.github.ardoco.core.neo4jschema.mapper.ArchitectureModelMapper;
import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureModelRepository;

@Service
public class ArchitecturePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(ArchitecturePersistenceService.class);

    private final ArchitectureModelRepository repository;
    private final ArchitectureModelMapper mapper;
    private final Neo4jClient neo4jClient;

    public ArchitecturePersistenceService(ArchitectureModelRepository repository, ArchitectureModelMapper mapper, Neo4jClient neo4jClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.neo4jClient = neo4jClient;
    }

    /**
     * Deletes a specific architecture model by its metamodel type. Note: This assumes one model per metamodel type. If multiple models exist for the same
     * metamodel, it will delete all of them. If no model is found for the given metamodel, it does nothing.
     *
     * @param metamodel The metamodel type of the architecture model(s) to be deleted from the database.
     */
    @Transactional
    public void deleteArchitectureModel(Metamodel metamodel) {
        List<ArchitectureModelNode> nodes = repository.findAll();
        for (ArchitectureModelNode node : nodes) {
            if (metamodel.name().equals(node.getMetamodel())) {
                repository.deleteByModelId(node.getModelId());
            }
        }
    }

    /**
     * Saves the given architecture model. This method first deletes any existing model with the same ID to ensure that stale data is not retained. Then, it
     * converts the domain model into a node entity using the ArchitectureModelMapper and persists it via targeted, batched Cypher.
     *
     * @param model The domain ArchitectureModel object to be saved into the database.
     */
    @Transactional
    public void saveArchitectureModel(ArchitectureModel model) {
        repository.deleteByModelId(model.getId());
        ArchitectureModelNode modelNode = mapper.toNode(model);
        persistArchitectureModelBatched(modelNode);
    }

    private static final int BATCH_CHUNK_SIZE = 2000;

    /**
     * Persists the already-built {@link ArchitectureModelNode} graph (see {@link ArchitectureModelMapper#toNode}) via targeted, batched Cypher instead of
     * {@code repository.save(ArchitectureModelNode)}, following the code model's pattern ({@link CodePersistenceService#saveCodeModel}). {@code toNode}
     * already de-duplicates components/interfaces by {@code ardocoId}; this method only replaces the final persistence step.
     */
    private void persistArchitectureModelBatched(ArchitectureModelNode modelNode) {
        neo4jClient.query("CREATE (m:ArchitectureModel {id: $id, modelId: $modelId, metamodel: $metamodel})")
                .bind(UUID.randomUUID().toString())
                .to("id")
                .bind(modelNode.getModelId())
                .to("modelId")
                .bind(modelNode.getMetamodel())
                .to("metamodel")
                .run();

        // Components are only reachable from the model as top-level content; interfaces and methods can additionally be reachable only
        // transitively (e.g. an interface that is merely provided/required by a component, not a top-level model item) - collect both by walking
        // the graph, de-duplicated by Java identity (toNode's caches are keyed by ardocoId, so no two distinct nodes share one here).
        Collection<ArchitectureComponentNode> components = collectReachable(modelNode.getComponents(), ArchitectureComponentNode::getSubcomponents);

        Map<ArchitectureInterfaceNode, Boolean> interfaces = new IdentityHashMap<>();
        for (ArchitectureInterfaceNode iface : modelNode.getInterfaces()) {
            interfaces.put(iface, Boolean.TRUE);
        }
        for (ArchitectureComponentNode component : components) {
            for (ArchitectureInterfaceNode iface : component.getProvidedInterfaces()) {
                interfaces.put(iface, Boolean.TRUE);
            }
            for (ArchitectureInterfaceNode iface : component.getRequiredInterfaces()) {
                interfaces.put(iface, Boolean.TRUE);
            }
        }

        Map<ArchitectureMethodNode, Boolean> methods = new IdentityHashMap<>();
        for (ArchitectureInterfaceNode iface : interfaces.keySet()) {
            for (ArchitectureMethodNode method : iface.getMethodSignatures()) {
                methods.put(method, Boolean.TRUE);
            }
        }

        createNodesByLabel("ArchitectureComponent:ArchitectureItem:Traceable", componentProps(components));
        createNodesByLabel("ArchitectureInterface:ArchitectureItem:Traceable", interfaceProps(interfaces.keySet()));
        createNodesByLabel("ArchitectureMethod:ArchitectureItem:Traceable", methodProps(methods.keySet()));

        LinkTargetErrors errors = new LinkTargetErrors("ArchitectureModel " + modelNode.getModelId());
        createModelToItemEdges("HAS_COMPONENT", modelNode.getModelId(), modelNode.getComponents().stream().map(ArchitectureComponentNode::getArdocoId).toList(),
                errors);
        createModelToItemEdges("HAS_INTERFACE", modelNode.getModelId(), modelNode.getInterfaces().stream().map(ArchitectureInterfaceNode::getArdocoId).toList(),
                errors);

        List<Map<String, Object>> subcomponentEdges = new ArrayList<>();
        List<Map<String, Object>> providesEdges = new ArrayList<>();
        List<Map<String, Object>> requiresEdges = new ArrayList<>();
        for (ArchitectureComponentNode component : components) {
            addEdges(subcomponentEdges, component.getArdocoId(), component.getSubcomponents());
            addEdges(providesEdges, component.getArdocoId(), component.getProvidedInterfaces());
            addEdges(requiresEdges, component.getArdocoId(), component.getRequiredInterfaces());
        }
        List<Map<String, Object>> methodEdges = new ArrayList<>();
        for (ArchitectureInterfaceNode iface : interfaces.keySet()) {
            addEdges(methodEdges, iface.getArdocoId(), iface.getMethodSignatures());
        }

        createTraceableToTraceableEdges("HAS_SUBCOMPONENT", subcomponentEdges, errors);
        createTraceableToTraceableEdges("PROVIDES_INTERFACE", providesEdges, errors);
        createTraceableToTraceableEdges("REQUIRES_INTERFACE", requiresEdges, errors);
        createTraceableToTraceableEdges("HAS_METHOD", methodEdges, errors);
        errors.throwIfAny(logger);
    }

    /** Breadth-first walk from {@code roots}, following {@code children}, de-duplicated by Java identity. */
    private static <T> Collection<T> collectReachable(Collection<T> roots, java.util.function.Function<T, Collection<T>> children) {
        Map<T, Boolean> visited = new IdentityHashMap<>();
        Deque<T> stack = new ArrayDeque<>(roots);
        while (!stack.isEmpty()) {
            T node = stack.pop();
            if (visited.put(node, Boolean.TRUE) != null) {
                continue;
            }
            stack.addAll(children.apply(node));
        }
        return visited.keySet();
    }

    private static Collection<Map<String, Object>> componentProps(Collection<ArchitectureComponentNode> components) {
        List<Map<String, Object>> props = new ArrayList<>(components.size());
        for (ArchitectureComponentNode c : components) {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("ardocoId", c.getArdocoId());
            putIfNotNull(item, "name", c.getName());
            putIfNotNull(item, "type", c.getType());
            props.add(item);
        }
        return props;
    }

    private static Collection<Map<String, Object>> interfaceProps(Collection<ArchitectureInterfaceNode> interfaces) {
        List<Map<String, Object>> props = new ArrayList<>(interfaces.size());
        for (ArchitectureInterfaceNode i : interfaces) {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("ardocoId", i.getArdocoId());
            putIfNotNull(item, "name", i.getName());
            putIfNotNull(item, "type", i.getType());
            props.add(item);
        }
        return props;
    }

    private static Collection<Map<String, Object>> methodProps(Collection<ArchitectureMethodNode> methods) {
        List<Map<String, Object>> props = new ArrayList<>(methods.size());
        for (ArchitectureMethodNode m : methods) {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("ardocoId", m.getArdocoId());
            putIfNotNull(item, "name", m.getName());
            props.add(item);
        }
        return props;
    }

    private static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    private static void addEdges(List<Map<String, Object>> edges, String sourceId, Collection<? extends ArchitectureItemNode> targets) {
        for (ArchitectureItemNode target : targets) {
            edges.add(Map.of("sourceId", sourceId, "targetId", target.getArdocoId()));
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
                    """.formatted(labels)).bind(chunk).to("items").run();
        }
    }

    private void createModelToItemEdges(String relationshipType, String modelId, List<String> itemIds, LinkTargetErrors errors) {
        if (itemIds.isEmpty()) {
            return;
        }
        for (List<String> idChunk : chunk(itemIds, BATCH_CHUNK_SIZE)) {
            List<Map<String, Object>> unresolved = new ArrayList<>(neo4jClient.query("""
                    MATCH (m:ArchitectureModel {modelId: $modelId})
                    UNWIND range(0, size($itemIds) - 1) AS index
                    WITH m, index, $itemIds[index] AS itemId
                    OPTIONAL MATCH (n:ArchitectureItem:Traceable {ardocoId: itemId}) USING INDEX SEEK n:Traceable(ardocoId)
                    WITH m, index, itemId, collect(n) AS targets
                    FOREACH (target IN CASE WHEN size(targets) = 1 THEN targets ELSE [] END | CREATE (m)-[:%s]->(target))
                    WITH itemId, size(targets) AS targetMatches
                    WHERE targetMatches <> 1
                    RETURN $modelId AS sourceId, itemId AS targetId, 1 AS sourceMatches, targetMatches
                    """.formatted(relationshipType)).bind(modelId).to("modelId").bind(idChunk).to("itemIds").fetch().all());
            errors.recordEdges(relationshipType, unresolved);
        }
    }

    private void createTraceableToTraceableEdges(String relationshipType, List<Map<String, Object>> edges, LinkTargetErrors errors) {
        if (edges.isEmpty()) {
            return;
        }
        for (List<Map<String, Object>> chunk : chunk(edges, BATCH_CHUNK_SIZE)) {
            List<Map<String, Object>> unresolved = new ArrayList<>(neo4jClient.query("""
                    UNWIND range(0, size($edges) - 1) AS index
                    WITH index, $edges[index] AS edge
                    OPTIONAL MATCH (s:ArchitectureItem:Traceable {ardocoId: edge.sourceId}) USING INDEX SEEK s:Traceable(ardocoId)
                    WITH index, edge, collect(s) AS sources
                    OPTIONAL MATCH (t:ArchitectureItem:Traceable {ardocoId: edge.targetId}) USING INDEX SEEK t:Traceable(ardocoId)
                    WITH index, edge, sources, collect(t) AS targets
                    FOREACH (s IN CASE WHEN size(sources) = 1 AND size(targets) = 1 THEN sources ELSE [] END |
                        FOREACH (t IN targets | CREATE (s)-[:%s]->(t)))
                    WITH edge, size(sources) AS sourceMatches, size(targets) AS targetMatches
                    WHERE sourceMatches <> 1 OR targetMatches <> 1
                    RETURN edge.sourceId AS sourceId, edge.targetId AS targetId, sourceMatches, targetMatches
                    """.formatted(relationshipType)).bind(chunk).to("edges").fetch().all());
            errors.recordEdges(relationshipType, unresolved);
        }
    }

    private static <T> List<List<T>> chunk(List<T> items, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < items.size(); i += size) {
            chunks.add(items.subList(i, Math.min(i + size, items.size())));
        }
        return chunks;
    }

    /**
     * Loads an architecture model from the database based on the provided metamodel type.
     *
     * @param metamodel The metamodel type used to locate the ArchitectureModelNode in the database. This method assumes that there is at most one model per
     *                  metamodel type.
     * @return If multiple models exist for the same metamodel, it will return the first one found. If no model is found for the given metamodel, it returns
     *         null.
     */
    @Transactional(readOnly = true)
    public Optional<ArchitectureModel> loadArchitectureModel(Metamodel metamodel) {
        List<ArchitectureModelNode> nodes = repository.findAll();
        for (ArchitectureModelNode node : nodes) {
            if (node.getMetamodel().equals(metamodel.name())) {
                return Optional.of(mapper.toDomain(node));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves a sorted set of all metamodel types for which architecture models are currently stored in the database.
     *
     * @return A sorted set of Metamodel enums representing the types of architecture models available in the database. If no models are stored, it returns an
     *         empty set.
     */
    @Transactional(readOnly = true)
    public SortedSet<Metamodel> getStoredArchitectureModelMetamodels() {
        SortedSet<Metamodel> available = new java.util.TreeSet<>();
        List<ArchitectureModelNode> archNodes = repository.findAll();
        for (ArchitectureModelNode node : archNodes) {
            available.add(Metamodel.valueOf(node.getMetamodel()));
        }
        return available;
    }

}
