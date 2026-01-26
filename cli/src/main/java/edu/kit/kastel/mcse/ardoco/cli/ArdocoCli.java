/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the ARDoCo Command Line Interface. This class initializes the PluginManager which discovers
 * available task plugins via ServiceLoader and executes the requested task.
 *
 * <p>
 * Usage:
 *
 * <pre>
 * java -jar ardoco-cli.jar -t &lt;task&gt; -n &lt;name&gt; [options] -o &lt;output&gt;
 * </pre>
 *
 * <p>
 * Available tasks are discovered at runtime from plugins on the classpath.
 */
public final class ArdocoCli {

    private static final Logger logger = LoggerFactory.getLogger(ArdocoCli.class);

    private ArdocoCli() {
        // Prevent instantiation
    }

    /**
     * Main method for the ARDoCo CLI.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        logger.info("ARDoCo CLI ");
        logger.info("========================================");

        PluginManager pluginManager = new PluginManager();

        if (pluginManager.getPlugins().isEmpty()) {
            logger.error("No task plugins found on classpath. Ensure cli-plugins-tlr or other plugin JARs are available.");
            System.exit(1);
        }

        pluginManager.executePlugins(args);
    }
}
