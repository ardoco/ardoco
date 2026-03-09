/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service.architectureModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureComponentNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureInterfaceNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureMethodNode;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureModelNode;
import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureModelRepository;

@Service
public class ArchitecturePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(ArchitecturePersistenceService.class);

    private final ArchitectureModelRepository repository;
    private final ArchitectureModelMapper mapper;

    public ArchitecturePersistenceService(ArchitectureModelRepository repository, ArchitectureModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public void saveArchitectureModel(ArchitectureModel model) {
        // fist check if model with same id exists and delete it
//        repository.findByModelId(model.getId()).ifPresent(existingModel -> {
//            logger.info("Deleting existing Architecture Model with ID: {}", model.getId());
//            repository.delete(existingModel);
//        });
        logger.info("Preparing database for Architecture Model: {}", model.getId());
        repository.deleteByModelId(model.getId());

        logger.info("Start saving Architecture Model with ID: {}", model.getId());
        ArchitectureModelNode modelNode = new ArchitectureModelNode(model.getId(), model.getMetamodel().name());

        Map<String, ArchitectureComponentNode> componentCache = new HashMap<>();
        Map<String, ArchitectureInterfaceNode> interfaceCache = new HashMap<>();

        for (ArchitectureItem item : model.getContent()) {
            if (item instanceof ArchitectureComponent comp) {
                ArchitectureComponentNode compNode = mapComponent(comp, componentCache, interfaceCache);
                modelNode.addComponent(compNode);
            } else if (item instanceof ArchitectureInterface iface) {
                ArchitectureInterfaceNode ifaceNode = mapInterface(iface, interfaceCache);
                modelNode.addInterface(ifaceNode);
            }
        }

        repository.save(modelNode);
        logger.info("Finished saving Architecture Model with ID: {}", model.getId());
    }

    private ArchitectureComponentNode mapComponent(ArchitectureComponent domainComp, Map<String, ArchitectureComponentNode> compCache,
            Map<String, ArchitectureInterfaceNode> interfaceCache) {

        logger.info("Mapping Architecture Component: {} (ID: {})", domainComp.getName(), domainComp.getId());
        if (compCache.containsKey(domainComp.getId())) {
            return compCache.get(domainComp.getId());
        }

        ArchitectureComponentNode node = new ArchitectureComponentNode(domainComp.getName(), domainComp.getType().orElse(null), domainComp.getId());
        compCache.put(domainComp.getId(), node);

        // Map Subcomponents (Recursion)
        for (ArchitectureComponent sub : domainComp.getSubcomponents()) {
            node.addSubcomponent(mapComponent(sub, compCache, interfaceCache));
        }

        // Map Provided Interfaces
        for (ArchitectureInterface iface : domainComp.getProvidedInterfaces()) {
            node.addProvidedInterface(mapInterface(iface, interfaceCache));
        }

        // Map Required Interfaces
        for (ArchitectureInterface iface : domainComp.getRequiredInterfaces()) {
            node.addRequiredInterface(mapInterface(iface, interfaceCache));
        }

        return node;
    }

    private ArchitectureInterfaceNode mapInterface(ArchitectureInterface domainInterface, Map<String, ArchitectureInterfaceNode> ifaceCache) {
        logger.info("Mapping Architecture Interface: {} (ID: {})", domainInterface.getName(), domainInterface.getId());
        if (ifaceCache.containsKey(domainInterface.getId())) {
            return ifaceCache.get(domainInterface.getId());
        }

        ArchitectureInterfaceNode node = new ArchitectureInterfaceNode(domainInterface.getName(), domainInterface.getType().orElse(null), domainInterface
                .getId());
        ifaceCache.put(domainInterface.getId(), node);

        for (ArchitectureMethod method : domainInterface.getMethodSignatures()) {
            ArchitectureMethodNode methodNode = new ArchitectureMethodNode(method.getName(), method.getId());
            node.addMethodSignature(methodNode);
        }

        return node;
    }

    @Transactional(readOnly = true)
    public ArchitectureModel loadArchitectureModel(Metamodel metamodel) {
        List<ArchitectureModelNode> nodes = repository.findAll();
        for (ArchitectureModelNode node : nodes) {
            if (node.getMetamodel() != null && node.getMetamodel().equals(metamodel.name())) {
                return mapper.mapToDomain(node);
            }
        }
        logger.warn("No Architecture Model found for type: " + metamodel);
        return null;
    }

    @Transactional(readOnly = true)
    public SortedSet<Metamodel> getStoredArchitectureModelMetamodels() {
        SortedSet<Metamodel> available = new java.util.TreeSet<>();
        List<ArchitectureModelNode> archNodes = repository.findAll();
        for (ArchitectureModelNode node : archNodes) {
            try {
                available.add(Metamodel.valueOf(node.getMetamodel()));
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown metamodel found in stored Architecture Models: " + node.getMetamodel());
            }
        }
        return available;
    }

}
