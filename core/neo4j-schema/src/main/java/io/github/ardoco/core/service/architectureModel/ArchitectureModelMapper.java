package io.github.ardoco.core.service.architectureModel;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureComponentModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import io.github.ardoco.core.Neo4jPersistenceHandler;
import io.github.ardoco.core.entities.architectureModel.ArchitectureComponentNode;
import io.github.ardoco.core.entities.architectureModel.ArchitectureInterfaceNode;
import io.github.ardoco.core.entities.architectureModel.ArchitectureMethodNode;
import io.github.ardoco.core.entities.architectureModel.ArchitectureModelNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ArchitectureModelMapper {

    private static final Logger logger = LoggerFactory.getLogger(ArchitectureModelMapper.class);

    /**
     * Reconstructs the domain model from the Neo4j graph nodes.
     */
    public ArchitectureModel mapToDomain(ArchitectureModelNode modelNode) {
        logger.info("Start mapping ArchitectureModelNode with ID {} to domain model.", modelNode.getModelId());
        // Caches to ensure object identity (Singleton per ID)
        Map<String, ArchitectureInterface> interfaceCache = new HashMap<>();
        Map<String, ArchitectureComponent> componentCache = new HashMap<>();

        // Restore all Interfaces
        List<ArchitectureInterface> interfaces = new ArrayList<>();
        for (ArchitectureInterfaceNode ifaceNode : modelNode.getInterfaces()) {
            ArchitectureInterface domainInterface = mapInterface(ifaceNode, interfaceCache);
            interfaces.add(domainInterface);
        }

        // Restore all Components and subcomponents
        List<ArchitectureComponent> components = new ArrayList<>();
        for (ArchitectureComponentNode compNode : modelNode.getComponents()) {
            ArchitectureComponent domainComponent = mapComponent(compNode, componentCache, interfaceCache);
            components.add(domainComponent);
        }

        List<ArchitectureItem> content = new ArrayList<>();
        content.addAll(components);
        content.addAll(interfaces);

        ArchitectureModelWithComponentsAndInterfaces baseModel =
                new ArchitectureModelWithComponentsAndInterfaces(modelNode.getModelId(), content);

        String storedType = modelNode.getMetamodel();
        if (Metamodel.ARCHITECTURE_WITH_COMPONENTS.name().equals(storedType)) {
            return new ArchitectureComponentModel(baseModel);
        }

        return baseModel;
    }

    private ArchitectureInterface mapInterface(ArchitectureInterfaceNode node,
            Map<String, ArchitectureInterface> cache) {
        logger.info("Mapping ArchitectureInterfaceNode with ID {} to domain model.", node.getArdocoId());
        if (cache.containsKey(node.getArdocoId())) {
            return cache.get(node.getArdocoId());
        }

        SortedSet<ArchitectureMethod> methods = new TreeSet<>();
        for (ArchitectureMethodNode methodNode : node.getMethodSignatures()) {
            methods.add(new ArchitectureMethod(methodNode.getName()));
        }

        ArchitectureInterface domainInterface = new ArchitectureInterface(
                node.getName(),
                node.getArdocoId(),
                methods
        );

        cache.put(node.getArdocoId(), domainInterface);
        return domainInterface;
    }

    private ArchitectureComponent mapComponent(ArchitectureComponentNode node,
            Map<String, ArchitectureComponent> compCache,
            Map<String, ArchitectureInterface> ifaceCache) {
        logger.info("Mapping ArchitectureComponentNode with ID {} to domain model.", node.getArdocoId());
        if (compCache.containsKey(node.getArdocoId())) {
            return compCache.get(node.getArdocoId());
        }

        // Recursively map Subcomponents
        SortedSet<ArchitectureComponent> subcomponents = new TreeSet<>();
        for (ArchitectureComponentNode subNode : node.getSubcomponents()) {
            subcomponents.add(mapComponent(subNode, compCache, ifaceCache));
        }

        // Resolve Provided Interfaces
        SortedSet<ArchitectureInterface> provided = new TreeSet<>();
        for (ArchitectureInterfaceNode ifaceNode : node.getProvidedInterfaces()) {
            provided.add(mapInterface(ifaceNode, ifaceCache));
        }

        // Resolve Required Interfaces
        SortedSet<ArchitectureInterface> required = new TreeSet<>();
        for (ArchitectureInterfaceNode ifaceNode : node.getRequiredInterfaces()) {
            required.add(mapInterface(ifaceNode, ifaceCache));
        }

        ArchitectureComponent domainComponent = new ArchitectureComponent(
                node.getName(),
                node.getArdocoId(),
                subcomponents,
                provided,
                required,
                node.getType()
        );

        compCache.put(node.getArdocoId(), domainComponent);
        return domainComponent;
    }
}
