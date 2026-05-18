/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.List;
import java.util.Optional;
import java.util.SortedSet;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import io.github.ardoco.core.neo4jschema.entities.architectureModel.ArchitectureModelNode;
import io.github.ardoco.core.neo4jschema.mapper.ArchitectureModelMapper;
import io.github.ardoco.core.neo4jschema.repository.architectureModel.ArchitectureModelRepository;

@Service
public class ArchitecturePersistenceService {

    private final ArchitectureModelRepository repository;
    private final ArchitectureModelMapper mapper;

    public ArchitecturePersistenceService(ArchitectureModelRepository repository, ArchitectureModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
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
     * converts the domain model into a node entity using the ArchitectureModelMapper and persists it using the ArchitectureModelRepository.
     *
     * @param model The domain ArchitectureModel object to be saved into the database.
     */
    @Transactional
    public void saveArchitectureModel(ArchitectureModel model) {
        repository.deleteByModelId(model.getId());
        ArchitectureModelNode modelNode = mapper.toNode(model);
        repository.save(modelNode);
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
