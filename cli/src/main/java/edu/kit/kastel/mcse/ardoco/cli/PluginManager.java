/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.collections.api.factory.SortedMaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.cli.TaskContext;
import edu.kit.kastel.mcse.ardoco.core.cli.TaskPlugin;

/**
 * Manages the discovery and execution of CLI task plugins. Uses Java's ServiceLoader mechanism to discover plugins at
 * runtime from the classpath.
 */
public class PluginManager {

    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    private final List<TaskPlugin> plugins;
    private final Options options;
    private final Map<String, TaskPlugin> taskNameToPlugin;

    /**
     * Creates a new PluginManager and discovers available plugins via ServiceLoader.
     */
    public PluginManager() {
        this.plugins = new ArrayList<>();
        this.options = new Options();
        this.taskNameToPlugin = new HashMap<>();

        discoverPlugins();
        addCommonOptions();
    }

    /**
     * Discovers plugins using Java's ServiceLoader mechanism.
     */
    private void discoverPlugins() {
        ServiceLoader<TaskPlugin> serviceLoader = ServiceLoader.load(TaskPlugin.class);

        for (TaskPlugin plugin : serviceLoader) {
            plugins.add(plugin);
            taskNameToPlugin.put(plugin.getTaskName().toLowerCase(), plugin);

            // Add plugin's options to global options
            for (Option option : plugin.getAllOptions()) {
                if (!options.hasOption(option.getOpt()) && !options.hasLongOption(option.getLongOpt())) {
                    options.addOption(option);
                }
            }

            logger.info("Discovered plugin: {} - {}", plugin.getTaskName(), plugin.getDescription());
        }

        if (plugins.isEmpty()) {
            logger.warn("No plugins discovered. Ensure plugin JARs are on the classpath.");
        }
    }

    /**
     * Adds common command-line options that are shared across all plugins.
     */
    private void addCommonOptions() {
        Option opt;

        // Help option
        opt = new Option("h", "help", false, "Show this help message");
        opt.setRequired(false);
        options.addOption(opt);

        // Output directory
        opt = new Option("o", "output", true, "Path to the output directory (required)");
        opt.setType(String.class);
        opt.setRequired(false);
        options.addOption(opt);

        // Task selection
        String availableTasks = String.join(", ", taskNameToPlugin.keySet());
        opt = new Option("t", "task", true, "Task to execute: " + availableTasks);
        opt.setType(String.class);
        opt.setRequired(false);
        options.addOption(opt);

        // Project name
        opt = new Option("n", "name", true, "Name of the project to analyze (required)");
        opt.setType(String.class);
        opt.setRequired(false);
        options.addOption(opt);
    }

    /**
     * Executes plugins based on command-line arguments.
     *
     * @param args command-line arguments
     * @return true if execution succeeded, false otherwise
     */
    public boolean executePlugins(String[] args) {
        CommandLine commandLine;
        try {
            commandLine = parseCommandLine(args);
        } catch (ParseException e) {
            logger.error("Failed to parse command line: {}", e.getMessage());
            printUsage();
            return false;
        }

        // Show help if requested
        if (commandLine.hasOption("h") || args.length == 0) {
            printUsage();
            return true; // Help is not a failure
        }

        // Validate required common options
        if (!commandLine.hasOption("o")) {
            logger.error("Missing required option: output directory (-o/--output)");
            printUsage();
            return false;
        }

        if (!commandLine.hasOption("t")) {
            logger.error("Missing required option: task (-t/--task)");
            printUsage();
            return false;
        }

        if (!commandLine.hasOption("n")) {
            logger.error("Missing required option: project name (-n/--name)");
            printUsage();
            return false;
        }

        // Set up output directory
        File outputDir = new File(commandLine.getOptionValue("o"));
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            logger.error("Failed to create output directory: {}", outputDir.getAbsolutePath());
            return false;
        }

        // Get project name
        String projectName = commandLine.getOptionValue("n");

        // Execute the selected task
        String taskValue = commandLine.getOptionValue("t");
        if (taskValue == null || taskValue.isBlank()) {
            logger.error("Task option provided but value is empty");
            return false;
        }
        String task = taskValue.toLowerCase();

        if (!taskNameToPlugin.containsKey(task)) {
            var availableTasks = String.join(", ", taskNameToPlugin.keySet());
            logger.error("Unknown task: {}. Available tasks: {}", task, availableTasks);
            return false;
        }

        TaskPlugin plugin = taskNameToPlugin.get(task);

        if (checkForMissingOptions(plugin, commandLine, task)) {
            return false;
        }

        // Create execution context
        TaskContext context = new TaskContext(projectName, outputDir, SortedMaps.immutable.empty());

        // Execute the plugin
        logger.info("Executing task: {}", task);
        try {
            plugin.execute(commandLine, context);
        } catch (Exception e) {
            logger.error("Task execution failed: {}", e.getMessage(), e);
            return false;
        }

        return true;
    }

    private boolean checkForMissingOptions(TaskPlugin plugin, CommandLine commandLine, String task) {
        var missingOptions = plugin.getRequiredOptions().select(it -> !commandLine.hasOption(it));
        for (Option opt : missingOptions) {
            logger.error("Missing required parameter for task '{}': {} (-{})", task, opt.getDescription(), opt.getOpt());
        }
        if (!missingOptions.isEmpty()) {
            logger.error("Parameter validation failed for task: {}", task);
            return true;
        }
        return false;
    }

    /**
     * Parses the command-line arguments.
     *
     * @param args the arguments
     * @return the parsed CommandLine
     * @throws ParseException if parsing fails
     */
    private CommandLine parseCommandLine(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
    }

    /**
     * Prints usage information including all available options and plugins.
     */
    private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        formatter.printHelp("java -jar ardoco-cli.jar", options, true);

        System.out.println("\nAvailable tasks:");
        for (TaskPlugin plugin : plugins) {
            System.out.printf("  %-12s %s%n", plugin.getTaskName(), plugin.getDescription());
        }

        System.out.println("\nExamples:");
        System.out.println("  java -jar ardoco-cli.jar -t sad-sam -n MyProject -d doc.txt -m model.repository -o ./output");
        System.out.println("  java -jar ardoco-cli.jar -t sam-code -n MyProject -m model.repository -c ./src -o ./output");
        System.out.println("  java -jar ardoco-cli.jar -t sad-code -n MyProject -d doc.txt -m model.repository -c ./src -o ./output");
    }

    /**
     * Gets the list of discovered plugins.
     *
     * @return list of plugins
     */
    public List<TaskPlugin> getPlugins() {
        return List.copyOf(plugins);
    }
}
