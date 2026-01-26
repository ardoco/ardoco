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
     */
    public void executePlugins(String[] args) {
        CommandLine cmd;
        try {
            cmd = parseCommandLine(args);
        } catch (ParseException e) {
            logger.error("Failed to parse command line: {}", e.getMessage());
            printUsage();
            return;
        }

        // Show help if requested
        if (cmd.hasOption("h") || args.length == 0) {
            printUsage();
            return;
        }

        // Validate required common options
        if (!cmd.hasOption("o")) {
            logger.error("Missing required option: output directory (-o/--output)");
            printUsage();
            return;
        }

        if (!cmd.hasOption("t")) {
            logger.error("Missing required option: task (-t/--task)");
            printUsage();
            return;
        }

        if (!cmd.hasOption("n")) {
            logger.error("Missing required option: project name (-n/--name)");
            printUsage();
            return;
        }

        // Set up output directory
        File outputDir = new File(cmd.getOptionValue("o"));
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Get project name
        String projectName = cmd.getOptionValue("n");

        // Execute the selected task
        String task = cmd.getOptionValue("t").toLowerCase();

        if (!taskNameToPlugin.containsKey(task)) {
            logger.error("Unknown task: {}. Available tasks: {}", task, String.join(", ", taskNameToPlugin.keySet()));
            return;
        }

        TaskPlugin plugin = taskNameToPlugin.get(task);

        if (!plugin.validateParameters(cmd)) {
            logger.error("Parameter validation failed for task: {}", task);
            return;
        }

        // Create execution context
        TaskContext context = new TaskContext(cmd, projectName, outputDir, SortedMaps.immutable.empty());

        // Execute the plugin
        logger.info("Executing task: {}", task);
        plugin.execute(context);

        // Cleanup temporary files
        cleanup(outputDir);
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
     * Cleans up temporary files created during execution.
     *
     * @param outputDir the output directory
     */
    private void cleanup(File outputDir) {
        String[] patternsToDelete = { "inconsistencyDetection_.*\\.txt", "traceLinks_.*\\.txt" };

        for (String pattern : patternsToDelete) {
            File[] files = outputDir.listFiles((dir, name) -> name.matches(pattern));
            if (files != null) {
                for (File file : files) {
                    if (file.delete()) {
                        logger.debug("Deleted temporary file: {}", file.getName());
                    }
                }
            }
        }
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
