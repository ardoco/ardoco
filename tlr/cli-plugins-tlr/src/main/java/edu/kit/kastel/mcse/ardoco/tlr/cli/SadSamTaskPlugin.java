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
import edu.kit.kastel.mcse.ardoco.tlr.execution.Swattr;
import edu.kit.kastel.mcse.ardoco.tlr.models.agents.ArchitectureConfiguration;

/**
 * Plugin for SAD-SAM (Software Architecture Documentation to Software Architecture Model) traceability link recovery.
 * Uses the Swattr runner to establish trace links between documentation and architecture model.
 */
@AutoService(TaskPlugin.class)
public class SadSamTaskPlugin extends TlrTaskPlugin {

    private static final Logger logger = LoggerFactory.getLogger(SadSamTaskPlugin.class);

    private static final String TASK_NAME = "sad-sam";

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

    @Override
    public String getDescription() {
        return "Recover trace links between software architecture documentation (SAD) and software architecture model (SAM)";
    }

    @Override
    public ImmutableList<Option> getRequiredOptions() {
        MutableList<Option> options = Lists.mutable.empty();
        optionDocumentation(options);
        optionModel(options);
        optionModelFormat(options);
        return options.toImmutable();
    }

    @Override
    public void execute(CommandLine commandLine, TaskContext context) {
        logger.info("Starting SAD-SAM traceability link recovery task (Swattr).");

        File documentation;
        File model;

        try {
            documentation = ensureFileExists(commandLine.getOptionValue(OPT_DOC));
            model = ensureFileExists(commandLine.getOptionValue(OPT_MODEL));
        } catch (IOException e) {
            logger.error("Error reading input files: {}", e.getMessage());
            return;
        }

        ModelFormat format = parseModelFormat(commandLine);
        ArchitectureConfiguration architectureConfig = new ArchitectureConfiguration(model, format);

        Swattr runner = new Swattr(context.projectName());
        runner.setUp(documentation, architectureConfig, context.additionalConfigs(), context.outputDirectory());
        runner.run();

        logger.info("SAD-SAM task completed.");
    }
}
