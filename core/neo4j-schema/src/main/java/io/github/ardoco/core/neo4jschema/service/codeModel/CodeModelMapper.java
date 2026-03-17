/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service.codeModel;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import edu.kit.kastel.mcse.ardoco.core.api.models.*;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.*;
import io.github.ardoco.core.neo4jschema.entities.codeModel.*;

@Component
public class CodeModelMapper {

    private static final Logger logger = LoggerFactory.getLogger(CodeModelMapper.class);

    /**
     * Maps a single CodeItemNode to a domain CodeItem.
     * Useful for restoring TraceLink endpoints.
     *
     * @param node the node to map
     * @return the domain object
     */
    public CodeItem mapItem(CodeItemNode node) {
        CodeItemRepository localRepo = new CodeItemRepository();
        CodeItem item = createInstance(node, localRepo);
        localRepo.init();

        return item;
    }

    public CodeModel mapToDomain(CodeModelNode node) {
        CodeItemRepository repository = new CodeItemRepository();

        // 1. Flatten the graph to find every single node involved (Recursive)
        Set<CodeItemNode> allNodes = new HashSet<>();
        collectAllNodes(node.getContent(), allNodes);

        // 2. Instantiate all items (Empty shells with IDs)
        for (CodeItemNode itemNode : allNodes) {
            if (!repository.containsCodeItem(itemNode.getArdocoId())) {
                createInstance(itemNode, repository);
            }
        }

        // 3. Second pass: Link content and cross-references
        for (CodeItemNode itemNode : allNodes) {
            CodeItem currentItem = repository.getCodeItem(itemNode.getArdocoId());
            if (currentItem == null) continue;

            // Link Containment (Hierarchy)
            for (CodeItemNode childNode : itemNode.getContent()) {
                CodeItem childItem = repository.getCodeItem(childNode.getArdocoId());
                if (childItem != null) {
                    linkChildToParent(currentItem, childItem);
                }
            }

            // Link Type Relationships (Inheritance/References)
            if (itemNode instanceof DatatypeNode dtNode && currentItem instanceof Datatype dtItem) {
                linkDatatypes(dtItem, dtNode, repository);
            }
        }

        repository.init();

        // 4. Wrap in appropriate model container
        SortedSet<CodeItem> roots = new TreeSet<>();
        for (CodeItemNode rootNode : node.getContent()) {
            roots.add(repository.getCodeItem(rootNode.getArdocoId()));
        }

        Metamodel mm = Metamodel.valueOf(node.getMetamodel());
        return mm == Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES
                ? new CodeModelWithCompilationUnitsAndPackages(node.getModelId(), repository, roots)
                : new CodeModelWithCompilationUnits(node.getModelId(), repository, roots);
    }

    private void collectAllNodes(List<CodeItemNode> currentNodes, Set<CodeItemNode> visited) {
        for (CodeItemNode node : currentNodes) {
            if (node == null || !visited.add(node)) continue;

            // Recurse into content
            collectAllNodes(node.getContent(), visited);

            // Recurse into Datatype links (Critical for cross-file references)
            if (node instanceof DatatypeNode dt) {
                collectAllNodes(new ArrayList<>(dt.getExtendedTypes()), visited);
                collectAllNodes(new ArrayList<>(dt.getImplementedTypes()), visited);
                collectAllNodes(new ArrayList<>(dt.getReferencedDatatypes()), visited);
            }
        }
    }

    private void linkDatatypes(Datatype domain, DatatypeNode node, CodeItemRepository repo) {
        // We map OUTGOING relationships from the DB to the domain object's ID lists
        domain.setExtendedTypes(mapNodesToTypes(node.getExtendedTypes(), repo));
        domain.setImplementedTypes(mapNodesToTypes(node.getImplementedTypes(), repo));
        domain.setDatatypeReference(mapNodesToTypes(node.getReferencedDatatypes(), repo));
    }

    private SortedSet<Datatype> mapNodesToTypes(Set<DatatypeNode> nodes, CodeItemRepository repo) {
        SortedSet<Datatype> result = new TreeSet<>();
        for (DatatypeNode n : nodes) {
            CodeItem item = repo.getCodeItem(n.getArdocoId());
            if (item instanceof Datatype dt) result.add(dt);
        }
        return result;
    }

    /**
     * Traverses the graph iteratively (BFS) to collect all unique nodes.
     */
    private List<CodeItemNode> flattenGraph(List<CodeItemNode> roots) {
        List<CodeItemNode> result = new ArrayList<>();
        Set<String> visitedIds = new HashSet<>();
        Queue<CodeItemNode> queue = new ArrayDeque<>(roots);

        while (!queue.isEmpty()) {
            CodeItemNode current = queue.poll();
            if (visitedIds.add(current.getArdocoId())) {
                result.add(current);

                if (current.getContent() != null) {
                    queue.addAll(current.getContent());
                }
                // Enqueue related types (for Datatypes) to ensure they exist
                if (current instanceof DatatypeNode dtNode) {
                    queue.addAll(dtNode.getExtendedTypes());
                    queue.addAll(dtNode.getImplementedTypes());
                }
            }
        }
        return result;
    }

    /**
     * Creates instances using the ID-aware constructors.
     * YOU MUST ENSURE THESE CONSTRUCTORS EXIST IN YOUR DOMAIN CLASSES.
     */
    private CodeItem createInstance(CodeItemNode node, CodeItemRepository repo) {
        String id = node.getArdocoId();

        if (node instanceof CodePackageNode p) {
            return new CodePackage(id, repo, p.getName());
        } else if (node instanceof CodeCompilationUnitNode c) {
            return new CodeCompilationUnit(id, repo, c.getName(), new TreeSet<>(), c.getPathElements(), c.getExtension(), ProgrammingLanguage.valueOf(c
                    .getLanguage()));
        } else if (node instanceof ClassUnitNode c) {
            return new ClassUnit(id, repo, c.getName(), new TreeSet<>());
        } else if (node instanceof InterfaceUnitNode i) {
            return new InterfaceUnit(id, repo, i.getName(), new TreeSet<>());
        } else if (node instanceof CodeAssemblyNode a) {
            return new CodeAssembly(id, repo, a.getName(), new TreeSet<>(), a.getLanguage());
        } else if (node instanceof ControlElementNode c) {
            return new ControlElement(id, repo, c.getName());
        }
        throw new IllegalArgumentException("Unknown node type: " + node.getClass().getSimpleName());
    }

    private void linkChildToParent(CodeItem parent, CodeItem child) {
        // Add Child to Parent's Content List
        switch (parent) {
            case CodeModule module -> module.addContent(child);
            case ClassUnit cls -> cls.addContent(child);
            case InterfaceUnit iface -> iface.addContent(child);
            default -> {
            }
        }

        // Set Child's Parent Reference
        switch (child) {
        case CodeModule childModule when parent instanceof CodeModule parentModule -> childModule.setParent(parentModule);
        case Datatype childDt when parent instanceof CodeCompilationUnit parentCu -> childDt.setCompilationUnit(parentCu);
        case Datatype childDt when parent instanceof Datatype parentDt -> childDt.setParentDatatype(parentDt);
        default -> {
        }
        }
    }

//    private void linkDatatypes(Datatype domain, DatatypeNode node, CodeItemRepository repo) {
//        SortedSet<Datatype> extended = new TreeSet<>();
//        for (DatatypeNode extNode : node.getExtendedTypes()) {
//            CodeItem item = repo.getCodeItem(extNode.getArdocoId());
//            if (item instanceof Datatype dt)
//                extended.add(dt);
//        }
//        domain.setExtendedTypes(extended);
//
//        SortedSet<Datatype> implemented = new TreeSet<>();
//        for (DatatypeNode implNode : node.getImplementedTypes()) {
//            CodeItem item = repo.getCodeItem(implNode.getArdocoId());
//            if (item instanceof Datatype dt)
//                implemented.add(dt);
//        }
//        domain.setImplementedTypes(implemented);
//
//        SortedSet<Datatype> referenced = new TreeSet<>();
//        for (DatatypeNode refNode : node.getReferencedDatatypes()) {
//            CodeItem item = repo.getCodeItem(refNode.getArdocoId());
//            if (item instanceof Datatype dt) {
//                referenced.add(dt);
//            }
//        }
//        domain.setDatatypeReference(referenced);
//    }
}
