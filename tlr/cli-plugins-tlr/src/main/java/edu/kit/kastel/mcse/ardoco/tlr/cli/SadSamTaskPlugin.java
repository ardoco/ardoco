/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.cli.TaskContext;
import edu.kit.kastel.mcse.ardoco.core.cli.TaskPlugin;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;

/**
 * Plugin for SAD-SAM (Software Architecture Documentation to Software Architecture Model) traceability link recovery.
 * Uses the Swattr runner to establish trace links between documentation and architecture model.
 */
@AutoService(TaskPlugin.class)
public class SadSamTaskPlugin implements TaskPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SadSamTaskPlugin.class);

    private static final String TASK_NAME = "sad-sam";
    private static final String OPT_DOC = "d";
    private static final String OPT_MODEL = "m";
    private static final String OPT_MODEL_FORMAT = "model-format";

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public String getDescription() {
        return "Recover trace links between software architecture documentation (SAD) and software architecture model (SAM)";
    }

    @Override
    public List<Option> getRequiredOptions() {
        List<Option> options = new ArrayList<>();

        Option opt = new Option(OPT_DOC, "documentation", true, "Path to the documentation file (SAD)");
        opt.setType(String.class);
        opt.setRequired(false); // Validation done by plugin
        options.add(opt);

        opt = new Option(OPT_MODEL, "model", true, "Path to the architecture model file (SAM)");
        opt.setType(String.class);
        opt.setRequired(false); // Validation done by plugin
        options.add(opt);

        opt = new Option(null, OPT_MODEL_FORMAT, true, "Model format: " + Arrays.toString(ModelFormat.values()));
        opt.setType(String.class);
        opt.setRequired(false); // Validation done by plugin
        options.add(opt);

        return options;
    }

    @Override
    public List<Option> getOptionalOptions() {
        return List.of();
    }

    @Override
    public boolean validateParameters(CommandLine cmd) {
        if (!cmd.hasOption(OPT_DOC)) {
            logger.error("Missing required parameter: documentation (-d)");
            return false;
        }
        if (!cmd.hasOption(OPT_MODEL)) {
            logger.error("Missing required parameter: model (-m)");
            return false;
        }
        if (!cmd.hasOption(OPT_MODEL_FORMAT)) {
            logger.error("Missing required parameter: model-format (--model-format)");
            return false;
        }
        return true;
    }

    @Override
    public void execute(TaskContext context) {
        logger.info("Starting SAD-SAM traceability link recovery task (Swattr).");

        CommandLine cmd = context.commandLine();
        File documentation;
        File model;

        try {
            documentation = ensureFileExists(cmd.getOptionValue(OPT_DOC));
            model = ensureFileExists(cmd.getOptionValue(OPT_MODEL));
        } catch (IOException e) {
            logger.error("Error reading input files: {}", e.getMessage());
            return;
        }

        ModelFormat format = parseModelFormat(cmd);
        ArchitectureConfiguration architectureConfig = new ArchitectureConfiguration(model, format);

        Swattr runner = new Swattr(context.projectName());
        runner.setUp(documentation, architectureConfig, context.additionalConfigs(), context.outputDirectory());
        runner.run();

        logger.info("SAD-SAM task completed.");
    }

    private File ensureFileExists(String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IOException("Path is null or empty");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("File does not exist: " + path);
        }
        return file;
    }

    private ModelFormat parseModelFormat(CommandLine cmd) {
        String formatStr = cmd.getOptionValue(OPT_MODEL_FORMAT).toUpperCase(Locale.ROOT);
        try {
            return ModelFormat.valueOf(formatStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Invalid model format '%s'. Valid values are: %s", formatStr, Arrays.toString(ModelFormat
                    .values())), e);
        }
    }
}
