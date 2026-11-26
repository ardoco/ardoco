/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ardoco.core.neo4jschema.entities.project.ProjectNode;
import io.github.ardoco.core.neo4jschema.repository.project.ProjectRepository;

@Service
public class ProjectPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectPersistenceService.class);

    private final ProjectRepository repository;

    public ProjectPersistenceService(ProjectRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void saveProjectMetadata(String projectName, String inputText) {
        ProjectNode node = repository.findByProjectName(projectName).orElseGet(() -> new ProjectNode(projectName));
        node.setInputText(inputText);
        repository.save(node);
        logger.debug("Saved Project metadata for {}", projectName);
    }

    @Transactional(readOnly = true)
    public boolean hasProjectMetadata(String projectName) {
        return repository.existsByProjectName(projectName);
    }

    @Transactional(readOnly = true)
    public Optional<String> loadProjectInputText(String projectName) {
        return repository.findByProjectName(projectName).map(ProjectNode::getInputText);
    }

    @Transactional(readOnly = true)
    public Optional<String> loadSoleProjectName() {
        var all = repository.findAll();
        var iterator = all.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        String name = iterator.next().getProjectName();
        if (iterator.hasNext()) {
            return Optional.empty();
        }
        return Optional.ofNullable(name);
    }
}
