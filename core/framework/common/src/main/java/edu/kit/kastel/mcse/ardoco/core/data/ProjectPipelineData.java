/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.data;

/**
 * {@link ProjectPipelineData} represents data that we know overall about the project such as the name of the project.
 */
public interface ProjectPipelineData extends PipelineStepData {
    String ID = "ProjectPipelineData";

    /**
     * Return the project name
     *
     * @return the project name
     */
    String getProjectName();
}
