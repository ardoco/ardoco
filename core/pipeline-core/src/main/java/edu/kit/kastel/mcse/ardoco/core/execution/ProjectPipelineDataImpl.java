/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import edu.kit.kastel.mcse.ardoco.core.data.ProjectPipelineData;

/**
 * Implementation of {@link ProjectPipelineData} that simply takes the project's name in the constructor to store it.
 */
public class ProjectPipelineDataImpl implements ProjectPipelineData {

    private static final long serialVersionUID = -993634357212795104L;
    private final String projectName;

    /**
     * Construct this class using the project's name
     *
     * @param projectName the project's name
     */
    ProjectPipelineDataImpl(String projectName) {
        super();
        this.projectName = projectName;
    }

    @Override
    public String getProjectName() {
        return this.projectName;
    }
}
