/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.List;
import java.util.SortedSet;

import io.github.ardoco.core.neo4jschema.mapper.CodeModelMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import io.github.ardoco.core.neo4jschema.entities.codeModel.CodeModelNode;
import io.github.ardoco.core.neo4jschema.repository.codeModel.CodeModelRepository;

@Service
public class CodePersistenceService {

    private final CodeModelRepository repository;
    private final CodeModelMapper mapper;

    public CodePersistenceService(CodeModelRepository repository, CodeModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
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
    public CodeModel loadCodeModel(Metamodel metamodel) throws IllegalArgumentException {
        List<CodeModelNode> nodes = repository.findAll();
        for (CodeModelNode node : nodes) {
            if (node.getMetamodel().equals(metamodel.name())) {
                return mapper.toDomain(node);
            }
        }
        throw new IllegalArgumentException("Unknown metamodel: " + metamodel.name());
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
