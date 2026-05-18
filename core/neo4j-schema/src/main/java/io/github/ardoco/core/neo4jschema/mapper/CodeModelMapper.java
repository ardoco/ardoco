/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.mapper;

import java.util.*;
import java.util.function.BiFunction;

import org.springframework.stereotype.Component;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnits;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelWithCompilationUnitsAndPackages;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.*;
import io.github.ardoco.core.neo4jschema.entities.codeModel.*;

@Component
public class CodeModelMapper {

    private static final Map<Class<? extends CodeItem>, BiFunction<CodeItem, String, CodeItemNode>> NODE_FACTORIES = Map.of(CodePackage.class, (item,
            id) -> new CodePackageNode(((CodePackage) item).getName(), id), CodeCompilationUnit.class, (item, id) -> {
                var c = (CodeCompilationUnit) item;
                return new CodeCompilationUnitNode(c.getName(), id, c.getExtension(), c.getLanguage().name(), c.getPathElements());
            }, ClassUnit.class, (item, id) -> new ClassUnitNode(((ClassUnit) item).getName(), id), InterfaceUnit.class, (item, id) -> new InterfaceUnitNode(
                    ((InterfaceUnit) item).getName(), id), CodeAssembly.class, (item, id) -> new CodeAssemblyNode(((CodeAssembly) item).getName(), id,
                            ((CodeAssembly) item).getLanguage()), ControlElement.class, (item, id) -> new ControlElementNode(((ControlElement) item).getName(),
                                    id));

    public CodeModelNode toNode(CodeModel model) {
        CodeModelNode modelNode = new CodeModelNode(model.getId(), model.getMetamodel().name());
        Map<String, CodeItemNode> cache = new HashMap<>();

        Set<String> modelContentIds = new HashSet<>(model.createCodeModelDto().content());
        CodeItemRepository itemRepo = model.createCodeModelDto().codeItemRepository();

        // Pass 1: Hierarchy
        for (String id : modelContentIds) {
            CodeItem item = itemRepo.getCodeItem(id);
            if (isRootInModel(item, modelContentIds)) {
                modelNode.addContent(mapHierarchyToNode(item, cache));
            }
        }

        // Pass 2: Type References
        cache.forEach((id, node) -> {
            if (node instanceof DatatypeNode dtNode && itemRepo.getCodeItem(id) instanceof Datatype dt) {
                linkTypeReferencesToNode(dt, dtNode, cache);
            }
        });

        return modelNode;
    }

    /**
     * Maps a single CodeItemNode. Useful for restoring specific TraceLink endpoints.
     */
    public CodeItem toDomain(CodeItemNode node) {
        CodeItemRepository localRepo = new CodeItemRepository();
        CodeItem item = instantiateDomainObject(node, localRepo);
        localRepo.init();
        return item;
    }

    /**
     * Maps an entire CodeModel tree.
     */
    public CodeModel toDomain(CodeModelNode node) {
        CodeItemRepository repository = new CodeItemRepository();
        Set<CodeItemNode> allNodes = new HashSet<>();

        // Iterative collection to prevent StackOverflow
        collectAllNodesIteratively(node.getContent(), allNodes);

        // Pass 1: Flat instantiation
        allNodes.forEach(n -> instantiateDomainObject(n, repository));

        // Pass 2: Linking
        for (CodeItemNode itemNode : allNodes) {
            CodeItem currentItem = repository.getCodeItem(itemNode.getArdocoId());
            if (currentItem == null)
                continue;

            // Containment
            for (CodeItemNode childNode : itemNode.getContent()) {
                CodeItem childItem = repository.getCodeItem(childNode.getArdocoId());
                if (childItem != null)
                    linkDomainHierarchy(currentItem, childItem);
            }

            // Cross-references
            if (itemNode instanceof DatatypeNode dtNode && currentItem instanceof Datatype dtItem) {
                linkDomainTypeReferences(dtItem, dtNode, repository);
            }
        }

        repository.init();
        return finalizeCodeModel(node, repository);
    }

    private void linkDomainHierarchy(CodeItem parent, CodeItem child) {
        if (parent instanceof CodeModule pm)
            pm.addContent(child);
        else if (parent instanceof ClassUnit pc)
            pc.addContent(child);
        else if (parent instanceof InterfaceUnit pi)
            pi.addContent(child);

        if (child instanceof CodeModule cm && parent instanceof CodeModule pm)
            cm.setParent(pm);
        else if (child instanceof Datatype dt) {
            if (parent instanceof CodeCompilationUnit cu)
                dt.setCompilationUnit(cu);
            else if (parent instanceof Datatype pd)
                dt.setParentDatatype(pd);
        }
    }

    private CodeItem instantiateDomainObject(CodeItemNode node, CodeItemRepository repo) {
        String id = node.getArdocoId();
        return switch (node) {
            case CodePackageNode p -> new CodePackage(id, repo, p.getName());
            case CodeCompilationUnitNode c -> new CodeCompilationUnit(id, repo, c.getName(), new TreeSet<>(), c.getPathElements(), c.getExtension(),
                    ProgrammingLanguage.valueOf(c.getLanguage()));
            case ClassUnitNode c -> new ClassUnit(id, repo, c.getName(), new TreeSet<>());
            case InterfaceUnitNode i -> new InterfaceUnit(id, repo, i.getName(), new TreeSet<>());
            case CodeAssemblyNode a -> new CodeAssembly(id, repo, a.getName(), new TreeSet<>(), a.getLanguage());
            case ControlElementNode c -> new ControlElement(id, repo, c.getName());
            default -> throw new IllegalArgumentException("Unknown node type: " + node.getClass().getSimpleName());
        };
    }

    private void collectAllNodesIteratively(List<CodeItemNode> startNodes, Set<CodeItemNode> visited) {
        Deque<CodeItemNode> stack = new ArrayDeque<>(startNodes);
        while (!stack.isEmpty()) {
            CodeItemNode node = stack.pop();
            if (node == null || !visited.add(node))
                continue;

            stack.addAll(node.getContent());
            if (node instanceof DatatypeNode dt) {
                stack.addAll(dt.getExtendedTypes());
                stack.addAll(dt.getImplementedTypes());
                stack.addAll(dt.getReferencedDatatypes());
            }
        }
    }

    private CodeItemNode mapHierarchyToNode(CodeItem item, Map<String, CodeItemNode> cache) {
        if (cache.containsKey(item.getId()))
            return cache.get(item.getId());
        CodeItemNode node = NODE_FACTORIES.entrySet()
                .stream()
                .filter(e -> e.getKey().isInstance(item))
                .map(e -> e.getValue().apply(item, item.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported: " + item.getClass()));

        cache.put(item.getId(), node);
        item.getContent().forEach(child -> node.addContent(mapHierarchyToNode(child, cache)));
        return node;
    }

    private void linkTypeReferencesToNode(Datatype dt, DatatypeNode node, Map<String, CodeItemNode> cache) {
        dt.getExtendedTypes().forEach(t -> node.addExtendedType((DatatypeNode) cache.get(t.getId())));
        dt.getImplementedTypes().forEach(t -> node.addImplementedType((DatatypeNode) cache.get(t.getId())));
        dt.getDatatypeReferences().forEach(t -> node.addReferencedDatatype((DatatypeNode) cache.get(t.getId())));
    }

    private void linkDomainTypeReferences(Datatype domain, DatatypeNode node, CodeItemRepository repo) {
        domain.setExtendedTypes(mapNodesToTypes(node.getExtendedTypes(), repo));
        domain.setImplementedTypes(mapNodesToTypes(node.getImplementedTypes(), repo));
        domain.setDatatypeReference(mapNodesToTypes(node.getReferencedDatatypes(), repo));
    }

    private SortedSet<Datatype> mapNodesToTypes(Set<DatatypeNode> nodes, CodeItemRepository repo) {
        SortedSet<Datatype> result = new TreeSet<>();
        for (DatatypeNode n : nodes) {
            if (repo.getCodeItem(n.getArdocoId()) instanceof Datatype dt)
                result.add(dt);
        }
        return result;
    }

    private boolean isRootInModel(CodeItem item, Set<String> modelContent) {
        if (item instanceof CodeModule cm)
            return !cm.hasParent() || !modelContent.contains(cm.getParent().getId());
        if (item instanceof Datatype dt) {
            return (dt.getCompilationUnit() == null || !modelContent.contains(dt.getCompilationUnit().getId())) && (dt
                    .getParentDatatype() == null || !modelContent.contains(dt.getParentDatatype().getId()));
        }
        return true;
    }

    private CodeModel finalizeCodeModel(CodeModelNode node, CodeItemRepository repository) {
        SortedSet<CodeItem> roots = new TreeSet<>();
        for (CodeItemNode rootNode : node.getContent()) {
            roots.add(repository.getCodeItem(rootNode.getArdocoId()));
        }
        Metamodel mm = Metamodel.valueOf(node.getMetamodel());
        return (mm == Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES) ?
                new CodeModelWithCompilationUnitsAndPackages(node.getModelId(), repository, roots) :
                new CodeModelWithCompilationUnits(node.getModelId(), repository, roots);
    }
}
