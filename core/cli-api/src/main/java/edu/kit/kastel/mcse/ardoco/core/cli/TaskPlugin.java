/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.core.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

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
 * <li>Implement the {@link #execute(CommandLine, TaskContext)} method to perform the actual task</li>
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
    ImmutableList<Option> getRequiredOptions();

    /**
     * Gets the list of optional command-line options for this plugin.
     *
     * @return list of optional options (defaults to empty list)
     */
    default ImmutableList<Option> getOptionalOptions() {
        return Lists.immutable.empty();
    }

    /**
     * Gets all options (required and optional) for this plugin.
     *
     * @return list of all options
     */
    default ImmutableList<Option> getAllOptions() {
        return getRequiredOptions().newWithAll(getOptionalOptions());
    }

    /**
     * Executes the plugin's task with the given context.
     *
     * @param commandLine the user-provided command line containing all options and arguments
     * @param context     the execution context containing command line, project name, output directory, etc.
     */
    void execute(CommandLine commandLine, TaskContext context);
}
