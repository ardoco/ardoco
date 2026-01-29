/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.cli.TaskContext;
import edu.kit.kastel.mcse.ardoco.core.cli.TaskPlugin;
import edu.kit.kastel.mcse.ardoco.tlr.execution.Arcotl;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.CodeConfiguration.CodeConfigurationType;

/**
 * Plugin for SAM-Code (Software Architecture Model to Code) traceability link recovery. Uses the Arcotl runner to
 * establish trace links between architecture model and source code.
 */
@AutoService(TaskPlugin.class)
public class SamCodeTaskPlugin implements TaskPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SamCodeTaskPlugin.class);

    private static final String TASK_NAME = "sam-code";
    private static final String OPT_MODEL = "m";
    private static final String OPT_CODE = "c";
    private static final String OPT_MODEL_FORMAT = "model-format";

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public String getDescription() {
        return "Recover trace links between software architecture model (SAM) and source code";
    }

    @Override
    public List<Option> getRequiredOptions() {
        List<Option> options = new ArrayList<>();

        Option opt = new Option(OPT_MODEL, "model", true, "Path to the architecture model file (SAM)");
        opt.setType(String.class);
        opt.setRequired(false); // Validation done by plugin
        options.add(opt);

        opt = new Option(OPT_CODE, "code", true, "Path to the source code directory");
        opt.setType(String.class);
        opt.setRequired(false); // Validation done by plugin
        options.add(opt);

        return options;
    }

    @Override
    public List<Option> getOptionalOptions() {
        List<Option> options = new ArrayList<>();

        Option opt = new Option(null, OPT_MODEL_FORMAT, true, "Model format: PCM, UML, COMPONENT_LISTING (auto-detected if not specified)");
        opt.setType(String.class);
        opt.setRequired(false);
        options.add(opt);

        return options;
    }

    @Override
    public boolean validateParameters(CommandLine cmd) {
        if (!cmd.hasOption(OPT_MODEL)) {
            logger.error("Missing required parameter: model (-m)");
            return false;
        }
        if (!cmd.hasOption(OPT_CODE)) {
            logger.error("Missing required parameter: code (-c)");
            return false;
        }
        return true;
    }

    @Override
    public void execute(TaskContext context) {
        logger.info("Starting SAM-Code traceability link recovery task (Arcotl).");

        CommandLine cmd = context.commandLine();
        File model;
        File codePath;

        try {
            model = ensureFileExists(cmd.getOptionValue(OPT_MODEL));
            codePath = ensurePathExists(cmd.getOptionValue(OPT_CODE));
        } catch (IOException e) {
            logger.error("Error reading input files: {}", e.getMessage());
            return;
        }

        ModelFormat format = detectOrParseModelFormat(cmd, model);
        ArchitectureConfiguration architectureConfig = new ArchitectureConfiguration(model, format);

        // Auto-detect if code path is a file (ACM) or directory (source code)
        CodeConfigurationType codeType = codePath.isDirectory() ? CodeConfigurationType.DIRECTORY : CodeConfigurationType.ACM_FILE;
        CodeConfiguration codeConfig = new CodeConfiguration(codePath, codeType);
        logger.info("Using code configuration type: {}", codeType);

        Arcotl runner = new Arcotl(context.projectName());
        runner.setUp(architectureConfig, codeConfig, context.additionalConfigs(), context.outputDirectory());
        runner.run();

        logger.info("SAM-Code task completed.");
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

    private File ensurePathExists(String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IOException("Path is null or empty");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("Path does not exist: " + path);
        }
        return file;
    }

    private ModelFormat detectOrParseModelFormat(CommandLine cmd, File modelFile) {
        if (cmd.hasOption(OPT_MODEL_FORMAT)) {
            String formatStr = cmd.getOptionValue(OPT_MODEL_FORMAT).toUpperCase();
            try {
                return ModelFormat.valueOf(formatStr);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.format("Invalid model format '%s'. Valid values are: %s", formatStr, Arrays.toString(ModelFormat
                        .values())), e);
            }
        }
        return ModelFormatDetector.detect(modelFile);
    }
}
