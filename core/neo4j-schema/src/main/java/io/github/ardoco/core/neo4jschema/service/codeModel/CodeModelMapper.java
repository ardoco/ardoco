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

        List<CodeItemNode> allNodes = flattenGraph(node.getContent());

        for (CodeItemNode itemNode : allNodes) {
            if (!repository.containsCodeItem(itemNode.getArdocoId())) {
                createInstance(itemNode, repository);
            }
        }

        // Link items
        for (CodeItemNode itemNode : allNodes) {
            CodeItem parentItem = repository.getCodeItem(itemNode.getArdocoId());
            if (parentItem == null)
                continue;

            // Link Content (Children)
            for (CodeItemNode childNode : itemNode.getContent()) {
                CodeItem childItem = repository.getCodeItem(childNode.getArdocoId());
                if (childItem != null) {
                    linkChildToParent(parentItem, childItem);
                } else {
                    logger.warn("Child item {} not found for parent {}", childNode.getArdocoId(), parentItem.getName());
                }
            }

            // Link Datatype Relationships (Extends/Implements)
            if (itemNode instanceof DatatypeNode dtNode && parentItem instanceof Datatype dtItem) {
                linkDatatypes(dtItem, dtNode, repository);
            }
        }

        repository.init();

        // 6. Construct CodeModel container
        SortedSet<CodeItem> rootItems = new TreeSet<>();
        for (CodeItemNode rootItemNode : node.getContent()) {
            rootItems.add(repository.getCodeItem(rootItemNode.getArdocoId()));
        }

        logger.info("mapped model to domain with {} code items", repository.getRepository().size());
        if (Metamodel.CODE_WITH_COMPILATION_UNITS.name().equals(node.getMetamodel())) {
            return new CodeModelWithCompilationUnits(node.getModelId(), repository, rootItems);
        } else if (Metamodel.CODE_WITH_COMPILATION_UNITS_AND_PACKAGES.name().equals(node.getMetamodel())) {
            return new CodeModelWithCompilationUnitsAndPackages(node.getModelId(), repository, rootItems);
        }

        throw new UnsupportedOperationException("Unknown CodeModel Metamodel: " + node.getMetamodel());
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
        if (child instanceof CodeModule childModule && parent instanceof CodeModule parentModule) {
            childModule.setParent(parentModule);
        } else if (child instanceof Datatype childDt && parent instanceof CodeCompilationUnit parentCu) {
            childDt.setCompilationUnit(parentCu);
        } else if (child instanceof Datatype childDt && parent instanceof Datatype parentDt) {
            childDt.setParentDatatype(parentDt);
        }
    }

    private void linkDatatypes(Datatype domain, DatatypeNode node, CodeItemRepository repo) {
        SortedSet<Datatype> extended = new TreeSet<>();
        for (DatatypeNode extNode : node.getExtendedTypes()) {
            CodeItem item = repo.getCodeItem(extNode.getArdocoId());
            if (item instanceof Datatype dt)
                extended.add(dt);
        }
        domain.setExtendedTypes(extended);

        SortedSet<Datatype> implemented = new TreeSet<>();
        for (DatatypeNode implNode : node.getImplementedTypes()) {
            CodeItem item = repo.getCodeItem(implNode.getArdocoId());
            if (item instanceof Datatype dt)
                implemented.add(dt);
        }
        domain.setImplementedTypes(implemented);
    }
}
