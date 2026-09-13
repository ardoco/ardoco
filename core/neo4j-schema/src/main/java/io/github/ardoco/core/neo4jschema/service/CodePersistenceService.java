/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;

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
        String modelId = neo4jClient.query("MATCH (m:CodeModel {metamodel: $mt}) RETURN m.modelId AS modelId LIMIT 1")
                .bind(metamodel.name())
                .to("mt")
                .fetchAs(String.class)
                .mappedBy((typeSystem, record) -> record.get("modelId").asString(null))
                .one()
                .orElse(null);
        if (modelId == null) {
            return Optional.empty();
        }

        // 1) Every code item that belongs to the model (via HAS_REPOSITORY_ITEM), independent of hierarchy reachability.
        Collection<Map<String, Object>> itemRows = neo4jClient.query(
                """
                        MATCH (m:CodeModel {metamodel: $mt})-[:HAS_REPOSITORY_ITEM]->(n:CodeItem)
                        RETURN n.ardocoId AS id, labels(n) AS labels, properties(n) AS props
                        """)
                .bind(metamodel.name())
                .to("mt")
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
                "MATCH (m:CodeModel {metamodel: $mt})-[:CONTAINS_CODE_ROOT]->(root:CodeItem) RETURN root.ardocoId AS rootId")
                .bind(metamodel.name())
                .to("mt")
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
     *
     * @param model The domain CodeModel object to be saved into the database.
     */
    @Transactional
    public void saveCodeModel(CodeModel model) {
        repository.deleteByModelId(model.getId());
        CodeModelNode modelNode = mapper.toNode(model);
        repository.save(modelNode);
    }

}
