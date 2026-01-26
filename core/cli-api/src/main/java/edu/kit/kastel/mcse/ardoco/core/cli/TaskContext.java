/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.cli;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;

/**
 * Immutable context object passed to {@link TaskPlugin#execute(TaskContext)} containing all information needed to
 * execute a CLI task.
 *
 * @param commandLine       the parsed command line containing all options and arguments
 * @param projectName       the name of the project being analyzed
 * @param outputDirectory   the directory where output files should be written
 * @param additionalConfigs additional configuration options as key-value pairs
 */
public record TaskContext(CommandLine commandLine, String projectName, File outputDirectory, ImmutableSortedMap<String, String> additionalConfigs) {

    /**
     * Creates a new TaskContext.
     *
     * @param commandLine       the parsed command line
     * @param projectName       the project name
     * @param outputDirectory   the output directory
     * @param additionalConfigs additional configurations
     */
    public TaskContext {
        if (commandLine == null) {
            throw new IllegalArgumentException("commandLine must not be null");
        }
        if (projectName == null || projectName.isBlank()) {
            throw new IllegalArgumentException("projectName must not be null or blank");
        }
        if (outputDirectory == null) {
            throw new IllegalArgumentException("outputDirectory must not be null");
        }
        if (additionalConfigs == null) {
            throw new IllegalArgumentException("additionalConfigs must not be null");
        }
    }
}
