/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.stereotype.Component;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureComponentModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureComponentNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureInterfaceNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureItemNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureMethodNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureModelNode;

@Component
public class ArchitectureModelMapper {

    /**
     * Maps a single ArchitectureItemNode to a domain ArchitectureItem. Useful for restoring TraceLink endpoints.
     *
     * @param node the node to map
     * @return the domain object
     */
    public ArchitectureItem mapItem(ArchitectureItemNode node) {
        if (node instanceof ArchitectureComponentNode componentNode) {
            return mapComponentToDomain(componentNode, new HashMap<>(), new HashMap<>());
        } else if (node instanceof ArchitectureInterfaceNode interfaceNode) {
            return mapInterfaceToDomain(interfaceNode, new HashMap<>());
        } else if (node instanceof ArchitectureMethodNode methodNode) {
            return new ArchitectureMethod(methodNode.getName(), methodNode.getArdocoId());
        }
        throw new IllegalArgumentException("Unknown ArchitectureItemNode type: " + node.getClass().getSimpleName());
    }

    /**
     * Reconstructs the domain model from the Neo4j graph nodes.
     */
    public ArchitectureModel toDomain(ArchitectureModelNode modelNode) {
        Map<String, ArchitectureInterface> interfaceCache = new HashMap<>();
        Map<String, ArchitectureComponent> componentCache = new HashMap<>();

        // Restore all Interfaces
        List<ArchitectureInterface> interfaces = new ArrayList<>();
        for (ArchitectureInterfaceNode ifaceNode : modelNode.getInterfaces()) {
            ArchitectureInterface domainInterface = mapInterfaceToDomain(ifaceNode, interfaceCache);
            interfaces.add(domainInterface);
        }

        // Restore all Components and subcomponents
        List<ArchitectureComponent> components = new ArrayList<>();
        for (ArchitectureComponentNode compNode : modelNode.getComponents()) {
            ArchitectureComponent domainComponent = mapComponentToDomain(compNode, componentCache, interfaceCache);
            components.add(domainComponent);
        }

        List<ArchitectureItem> content = new ArrayList<>();
        content.addAll(components);
        content.addAll(interfaces);

        ArchitectureModelWithComponentsAndInterfaces baseModel = new ArchitectureModelWithComponentsAndInterfaces(modelNode.getModelId(), content);

        String storedType = modelNode.getMetamodel();
        if (Metamodel.ARCHITECTURE_WITH_COMPONENTS.name().equals(storedType)) {
            return new ArchitectureComponentModel(modelNode.getModelId(), baseModel);
        }

        return baseModel;
    }

    public ArchitectureModelNode toNode(ArchitectureModel model) {
        ArchitectureModelNode node = new ArchitectureModelNode(model.getId(), model instanceof ArchitectureComponentModel ?
                Metamodel.ARCHITECTURE_WITH_COMPONENTS.name() :
                Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES.name());

        Map<String, ArchitectureComponentNode> componentCache = new HashMap<>();
        Map<String, ArchitectureInterfaceNode> interfaceCache = new HashMap<>();

        for (ArchitectureItem item : model.getContent()) {
            if (item instanceof ArchitectureComponent comp) {
                node.addComponent(mapComponentToNode(comp, componentCache, interfaceCache));
            } else if (item instanceof ArchitectureInterface iface) {
                node.addInterface(mapInterfaceToNode(iface, interfaceCache));
            }
        }
        return node;
    }

    private ArchitectureInterface mapInterfaceToDomain(ArchitectureInterfaceNode node, Map<String, ArchitectureInterface> cache) {
        if (cache.containsKey(node.getArdocoId())) {
            return cache.get(node.getArdocoId());
        }

        SortedSet<ArchitectureMethod> methods = new TreeSet<>();
        for (ArchitectureMethodNode methodNode : node.getMethodSignatures()) {
            methods.add(new ArchitectureMethod(methodNode.getArdocoId(), methodNode.getName()));
        }

        ArchitectureInterface domainInterface = new ArchitectureInterface(node.getName(), node.getArdocoId(), methods);

        cache.put(node.getArdocoId(), domainInterface);
        return domainInterface;
    }

    private ArchitectureComponent mapComponentToDomain(ArchitectureComponentNode node, Map<String, ArchitectureComponent> compCache,
            Map<String, ArchitectureInterface> ifaceCache) {
        if (compCache.containsKey(node.getArdocoId())) {
            return compCache.get(node.getArdocoId());
        }

        // Recursively map Subcomponents
        SortedSet<ArchitectureComponent> subcomponents = new TreeSet<>();
        for (ArchitectureComponentNode subNode : node.getSubcomponents()) {
            subcomponents.add(mapComponentToDomain(subNode, compCache, ifaceCache));
        }

        // Resolve Provided Interfaces
        SortedSet<ArchitectureInterface> provided = new TreeSet<>();
        for (ArchitectureInterfaceNode ifaceNode : node.getProvidedInterfaces()) {
            provided.add(mapInterfaceToDomain(ifaceNode, ifaceCache));
        }

        // Resolve Required Interfaces
        SortedSet<ArchitectureInterface> required = new TreeSet<>();
        for (ArchitectureInterfaceNode ifaceNode : node.getRequiredInterfaces()) {
            required.add(mapInterfaceToDomain(ifaceNode, ifaceCache));
        }

        ArchitectureComponent domainComponent = new ArchitectureComponent(node.getName(), node.getArdocoId(), subcomponents, provided, required,
                node.getType());

        compCache.put(node.getArdocoId(), domainComponent);
        return domainComponent;
    }

    private ArchitectureComponentNode mapComponentToNode(ArchitectureComponent domainComp, Map<String, ArchitectureComponentNode> compCache,
            Map<String, ArchitectureInterfaceNode> interfaceCache) {

        if (compCache.containsKey(domainComp.getId())) {
            return compCache.get(domainComp.getId());
        }

        ArchitectureComponentNode node = new ArchitectureComponentNode(domainComp.getName(), domainComp.getType().orElse(null), domainComp.getId());
        compCache.put(domainComp.getId(), node);

        // Map Subcomponents (Recursion)
        for (ArchitectureComponent sub : domainComp.getSubcomponents()) {
            node.addSubcomponent(mapComponentToNode(sub, compCache, interfaceCache));
        }

        // Map Provided Interfaces
        for (ArchitectureInterface iface : domainComp.getProvidedInterfaces()) {
            node.addProvidedInterface(mapInterfaceToNode(iface, interfaceCache));
        }

        // Map Required Interfaces
        for (ArchitectureInterface iface : domainComp.getRequiredInterfaces()) {
            node.addRequiredInterface(mapInterfaceToNode(iface, interfaceCache));
        }

        return node;
    }

    private ArchitectureInterfaceNode mapInterfaceToNode(ArchitectureInterface domainInterface, Map<String, ArchitectureInterfaceNode> ifaceCache) {
        if (ifaceCache.containsKey(domainInterface.getId())) {
            return ifaceCache.get(domainInterface.getId());
        }

        ArchitectureInterfaceNode node = new ArchitectureInterfaceNode(domainInterface.getName(), domainInterface.getType().orElse(null),
                domainInterface.getId());
        ifaceCache.put(domainInterface.getId(), node);

        for (ArchitectureMethod method : domainInterface.getMethodSignatures()) {
            ArchitectureMethodNode methodNode = new ArchitectureMethodNode(method.getName(), method.getId());
            node.addMethodSignature(methodNode);
        }
        return node;
    }
}
