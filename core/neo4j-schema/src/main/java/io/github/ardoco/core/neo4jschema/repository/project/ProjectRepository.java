/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.repository.project;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import io.github.ardoco.core.neo4jschema.entities.project.ProjectNode;

@Repository
public interface ProjectRepository extends Neo4jRepository<ProjectNode, String> {

    Optional<ProjectNode> findByProjectName(String projectName);

    boolean existsByProjectName(String projectName);
}
