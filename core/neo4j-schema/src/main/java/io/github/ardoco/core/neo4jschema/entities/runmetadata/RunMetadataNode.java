/* Licensed under MIT 2026. */
package io.github.ardoco.core.neo4jschema.entities.runmetadata;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("RunMetadata")
public class RunMetadataNode {

    @Id
    private String projectName;

    private String startTimestamp;

    private String finishTimestamp;

    private String configurationJson;

    public RunMetadataNode() {
    }

    public RunMetadataNode(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(String startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getFinishTimestamp() {
        return finishTimestamp;
    }

    public void setFinishTimestamp(String finishTimestamp) {
        this.finishTimestamp = finishTimestamp;
    }

    public String getConfigurationJson() {
        return configurationJson;
    }

    public void setConfigurationJson(String configurationJson) {
        this.configurationJson = configurationJson;
    }
}
