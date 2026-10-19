/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.ardoco.core.neo4jschema.entities.runmetadata.RunMetadataNode;
import io.github.ardoco.core.neo4jschema.repository.runmetadata.RunMetadataRepository;

@Service
public class RunMetadataPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(RunMetadataPersistenceService.class);

    private final RunMetadataRepository repository;

    public RunMetadataPersistenceService(RunMetadataRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void saveRunMetadataStart(String projectName, String startTimestamp, String configurationJson) {
        RunMetadataNode node = new RunMetadataNode(projectName);
        node.setStartTimestamp(startTimestamp);
        node.setConfigurationJson(configurationJson);
        repository.save(node);
        logger.debug("Saved run metadata start for {}", projectName);
    }

    @Transactional
    public void saveRunMetadataFinish(String projectName, String finishTimestamp) {
        repository.findById(projectName).ifPresent(node -> {
            node.setFinishTimestamp(finishTimestamp);
            repository.save(node);
        });
        logger.debug("Saved run metadata finish for {}", projectName);
    }

    @Transactional(readOnly = true)
    public Optional<RunMetadataNode> loadRunMetadata(String projectName) {
        return repository.findById(projectName);
    }
}
