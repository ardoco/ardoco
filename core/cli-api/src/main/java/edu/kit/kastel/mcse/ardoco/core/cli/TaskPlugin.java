/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.cli;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/**
 * Service Provider Interface (SPI) for ARDoCo CLI task plugins. Implementations of this interface are discovered at
 * runtime via {@link java.util.ServiceLoader} and provide specific TLR task functionality to the CLI.
 *
 * <p>
 * Plugin implementations should:
 * <ul>
 * <li>Be registered in {@code META-INF/services/edu.kit.kastel.mcse.ardoco.core.cli.TaskPlugin}</li>
 * <li>Have a public no-argument constructor</li>
 * <li>Define their required and optional command-line options</li>
 * <li>Implement the {@link #execute(TaskContext)} method to perform the actual task</li>
 * </ul>
 */
public interface TaskPlugin {

    /**
     * Gets the unique task name for this plugin. This name is used with the {@code -t/--task} command-line option to
     * select this plugin.
     *
     * @return the task name (e.g., "sad-sam", "sam-code", "sad-code")
     */
    String getTaskName();

    /**
     * Gets a human-readable description of what this plugin does.
     *
     * @return a brief description of the plugin's functionality
     */
    String getDescription();

    /**
     * Gets the list of required command-line options for this plugin. These options must be provided when this plugin
     * is selected.
     *
     * @return list of required options
     */
    List<Option> getRequiredOptions();

    /**
     * Gets the list of optional command-line options for this plugin.
     *
     * @return list of optional options (defaults to empty list)
     */
    default List<Option> getOptionalOptions() {
        return List.of();
    }

    /**
     * Gets all options (required and optional) for this plugin.
     *
     * @return list of all options
     */
    default List<Option> getAllOptions() {
        var all = new java.util.ArrayList<>(getRequiredOptions());
        all.addAll(getOptionalOptions());
        return all;
    }

    /**
     * Validates that all required parameters are present and valid.
     *
     * @param cmd the parsed command line
     * @return true if validation passes, false otherwise
     */
    boolean validateParameters(CommandLine cmd);

    /**
     * Executes the plugin's task with the given context.
     *
     * @param context the execution context containing command line, project name, output directory, etc.
     */
    void execute(TaskContext context);
}
