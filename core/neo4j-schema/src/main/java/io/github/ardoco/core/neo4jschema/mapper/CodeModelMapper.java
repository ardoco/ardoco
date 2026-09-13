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

        CodeItemRepository itemRepo = model.createCodeModelDto().codeItemRepository();
        Map<String, CodeItem> allItems = itemRepo.getRepository();

        // Pass 0: Instantiate a node for EVERY item in the repository. The repository - not the containment hierarchy reachable from the model's declared roots -
        // is the authoritative set of items. Relying on getContent() traversal from the roots silently dropped every item whose top-most ancestor was not part of
        // the model's declared content() (observed as ~58% data loss on real code models).
        for (CodeItem item : allItems.values()) {
            createNode(item, cache);
        }

        // Pass 1: Wire the containment hierarchy (CONTAINS_CODE_ITEM) and the explicit parent / compilation-unit / parent-datatype references for every item. The
        // latter are stored verbatim (not derived) because the domain keeps them independent of the content lists (see CodeItemNode#getParentModuleId()).
        for (Map.Entry<String, CodeItem> entry : allItems.entrySet()) {
            CodeItem item = entry.getValue();
            CodeItemNode itemNode = cache.get(entry.getKey());
            for (CodeItem child : item.getContent()) {
                CodeItemNode childNode = cache.get(child.getId());
                if (childNode != null) {
                    itemNode.addContent(childNode);
                }
            }
            // Persist the content order verbatim from the raw id list (not from getContent(), which drops unresolvable ids). The CONTAINS_CODE_ITEM relationship
            // above is unordered; see CodeItemNode#getContentIds().
            List<String> rawContentIds = switch (item) {
                case CodeModule module -> module.getRawContentIds();
                case ClassUnit classUnit -> classUnit.getContentIds();
                case InterfaceUnit interfaceUnit -> interfaceUnit.getContentIds();
                default -> null;
            };
            itemNode.setContentIds(rawContentIds == null ? null : new ArrayList<>(rawContentIds));
            if (item instanceof CodeModule module) {
                CodeModule parentModule = module.getParent();
                if (parentModule != null) {
                    itemNode.setParentModuleId(parentModule.getId());
                }
            }
            if (item instanceof Datatype datatype && itemNode instanceof DatatypeNode datatypeNode) {
                CodeCompilationUnit compilationUnit = datatype.getCompilationUnit();
                if (compilationUnit != null) {
                    datatypeNode.setCompilationUnitId(compilationUnit.getId());
                }
                Datatype parentDatatype = datatype.getParentDatatype();
                if (parentDatatype != null) {
                    datatypeNode.setParentDatatypeId(parentDatatype.getId());
                }
                // Persist the raw datatype-reference id lists verbatim (order and null-vs-empty preserved) for a lossless round trip.
                datatypeNode.setExtendedDataTypesIds(datatype.getExtendedDataTypesIds());
                datatypeNode.setImplementedDataTypesIds(datatype.getImplementedDataTypesIds());
                datatypeNode.setDatatypeReferencesIds(datatype.getDatatypeReferencesIds());
            }
        }

        // Pass 2: Roots (CONTAINS_CODE_ROOT) come from the model's declared content.
        Set<String> modelContentIds = new HashSet<>(model.createCodeModelDto().content());
        for (String id : modelContentIds) {
            CodeItem item = itemRepo.getCodeItem(id);
            if (item != null && isRootInModel(item, modelContentIds)) {
                modelNode.addContent(cache.get(id));
            }
        }

        // Pass 3: Type references (EXTENDS / IMPLEMENTS / REFERENCES_DATATYPE).
        cache.forEach((id, node) -> {
            if (node instanceof DatatypeNode dtNode && itemRepo.getCodeItem(id) instanceof Datatype dt) {
                linkTypeReferencesToNode(dt, dtNode, cache);
            }
        });

        // Pass 4: Attach every item to the model via HAS_REPOSITORY_ITEM so persistence retains the complete repository, independent of hierarchy reachability.
        for (CodeItemNode node : cache.values()) {
            modelNode.addRepositoryItem(node);
        }

        return modelNode;
    }

    /**
     * Instantiates (or returns the cached) {@link CodeItemNode} for the given domain item without recursing into its children.
     */
    private CodeItemNode createNode(CodeItem item, Map<String, CodeItemNode> cache) {
        return cache.computeIfAbsent(item.getId(), id -> NODE_FACTORIES.entrySet()
                .stream()
                .filter(e -> e.getKey().isInstance(item))
                .map(e -> e.getValue().apply(item, id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported: " + item.getClass())));
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

        // Iterative collection to prevent StackOverflow. Seed from both the roots (CONTAINS_CODE_ROOT) and the full repository set (HAS_REPOSITORY_ITEM) so that
        // items which are not reachable from a root through the containment hierarchy are still restored.
        List<CodeItemNode> seeds = new ArrayList<>(node.getContent());
        seeds.addAll(node.getAllRepositoryItems());
        collectAllNodesIteratively(seeds, allNodes);

        // Pass 1: Flat instantiation
        allNodes.forEach(n -> instantiateDomainObject(n, repository));

        // Pass 2: Linking
        for (CodeItemNode itemNode : allNodes) {
            CodeItem currentItem = repository.getCodeItem(itemNode.getArdocoId());
            if (currentItem == null)
                continue;

            // Containment (content lists), restored from the verbatim contentIds property. Prefer setting the raw id list directly so null-vs-empty and dangling
            // ids are preserved exactly (addContent would both initialize empty lists and drop null contentIds).
            List<String> contentIds = itemNode.getContentIds();
            if (currentItem instanceof CodeModule module) {
                module.setRawContentIds(copyOrNull(contentIds));
            } else if (currentItem instanceof ClassUnit classUnit) {
                // ClassUnit never uses a null content list in the domain (Jackson + constructors always initialize it to []).
                classUnit.setRawContentIds(contentIds == null ? new ArrayList<>() : new ArrayList<>(contentIds));
            } else if (currentItem instanceof InterfaceUnit interfaceUnit) {
                interfaceUnit.setRawContentIds(copyOrNull(contentIds));
            }

            // Explicit parent link (only if it was set in the original model).
            if (currentItem instanceof CodeModule module && itemNode.getParentModuleId() != null) {
                CodeItem parent = repository.getCodeItem(itemNode.getParentModuleId());
                if (parent instanceof CodeModule parentModule)
                    module.setParent(parentModule);
            }

            // Explicit datatype links + cross-references.
            if (itemNode instanceof DatatypeNode dtNode && currentItem instanceof Datatype dtItem) {
                if (dtNode.getCompilationUnitId() != null) {
                    CodeItem compilationUnit = repository.getCodeItem(dtNode.getCompilationUnitId());
                    if (compilationUnit instanceof CodeCompilationUnit codeCompilationUnit)
                        dtItem.setCompilationUnit(codeCompilationUnit);
                }
                if (dtNode.getParentDatatypeId() != null) {
                    CodeItem parentDatatype = repository.getCodeItem(dtNode.getParentDatatypeId());
                    if (parentDatatype instanceof Datatype parentDt)
                        dtItem.setParentDatatype(parentDt);
                }
                // Restore the raw datatype-reference id lists verbatim (see toNode). Copy the lists but preserve a null list as null, because the domain treats a
                // null list as distinct from an empty one in equals().
                dtItem.setExtendedDataTypesIds(copyOrNull(dtNode.getExtendedDataTypesIds()));
                dtItem.setImplementedDataTypesIds(copyOrNull(dtNode.getImplementedDataTypesIds()));
                dtItem.setDatatypeReferencesIds(copyOrNull(dtNode.getDatatypeReferencesIds()));
            }
        }

        repository.init();
        return finalizeCodeModel(node, repository);
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

    private void linkTypeReferencesToNode(Datatype dt, DatatypeNode node, Map<String, CodeItemNode> cache) {
        dt.getExtendedTypes().forEach(t -> node.addExtendedType((DatatypeNode) cache.get(t.getId())));
        dt.getImplementedTypes().forEach(t -> node.addImplementedType((DatatypeNode) cache.get(t.getId())));
        dt.getDatatypeReferences().forEach(t -> node.addReferencedDatatype((DatatypeNode) cache.get(t.getId())));
    }

    private static List<String> copyOrNull(List<String> ids) {
        return ids == null ? null : new ArrayList<>(ids);
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
