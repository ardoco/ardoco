/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.project;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * Root project metadata: name + raw input text (Phase 4 lightweight slice).
 */
@Node("Project")
public class ProjectNode {

    @Id
    private String projectName;

    private String inputText;

    public ProjectNode() {
    }

    public ProjectNode(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getInputText() {
        return inputText;
    }

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }
}
