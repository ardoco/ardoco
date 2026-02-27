/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
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
public class SamCodeTaskPlugin extends TlrTaskPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SamCodeTaskPlugin.class);

    private static final String TASK_NAME = "sam-code";

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public String getDescription() {
        return "Recover trace links between software architecture model (SAM) and source code";
    }

    @Override
    public ImmutableList<Option> getRequiredOptions() {
        MutableList<Option> options = Lists.mutable.empty();
        optionModel(options);
        optionCode(options);
        optionModelFormat(options);
        return options.toImmutable();
    }

    @Override
    public void execute(CommandLine commandLine, TaskContext context) {
        logger.info("Starting SAM-Code traceability link recovery task (Arcotl).");

        File model;
        File codePath;

        try {
            model = ensureFileExists(commandLine.getOptionValue(OPT_MODEL));
            codePath = ensurePathExists(commandLine.getOptionValue(OPT_CODE));
        } catch (IOException e) {
            logger.error("Error reading input files: {}", e.getMessage());
            return;
        }

        ModelFormat format = parseModelFormat(commandLine);
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
}
