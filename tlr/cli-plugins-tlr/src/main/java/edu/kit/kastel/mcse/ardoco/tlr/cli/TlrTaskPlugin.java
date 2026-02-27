/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.cli;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.eclipse.collections.api.list.MutableList;
import org.jspecify.annotations.Nullable;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;
import edu.kit.kastel.mcse.ardoco.core.cli.TaskPlugin;

abstract class TlrTaskPlugin implements TaskPlugin {
    protected static final String OPT_DOC = "d";
    protected static final String OPT_MODEL = "m";
    protected static final String OPT_CODE = "c";
    protected static final String OPT_MODEL_FORMAT = "model-format";

    protected final void optionModelFormat(MutableList<Option> options) {
        Option opt;
        opt = new Option(null, OPT_MODEL_FORMAT, true, "Model format: " + Arrays.toString(ModelFormat.values()));
        opt.setType(String.class);
        opt.setRequired(false); // Validation done by plugin
        options.add(opt);
    }

    protected final void optionCode(MutableList<Option> options) {
        Option opt;
        opt = new Option(OPT_CODE, "code", true, "Path to the source code directory");
        opt.setType(String.class);
        opt.setRequired(false); // Validation done by plugin
        options.add(opt);
    }

    protected final void optionModel(MutableList<Option> options) {
        Option opt;
        opt = new Option(OPT_MODEL, "model", true, "Path to the architecture model file (SAM)");
        opt.setType(String.class);
        opt.setRequired(false); // Validation done by plugin
        options.add(opt);
    }

    protected final void optionDocumentation(MutableList<Option> options) {
        Option opt = new Option(OPT_DOC, "documentation", true, "Path to the documentation file (SAD)");
        opt.setType(String.class);
        opt.setRequired(false); // Validation done by plugin
        options.add(opt);
    }

    protected final File ensureFileExists(@Nullable String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IOException("Path is null or empty");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("File does not exist: " + path);
        }
        return file;
    }

    protected final File ensurePathExists(@Nullable String path) throws IOException {
        if (path == null || path.isBlank()) {
            throw new IOException("Path is null or empty");
        }
        File file = new File(path);
        if (!file.exists()) {
            throw new IOException("Path does not exist: " + path);
        }
        return file;
    }

    protected final ModelFormat parseModelFormat(CommandLine cmd) {
        String formatStr = cmd.getOptionValue(OPT_MODEL_FORMAT).toUpperCase(Locale.ROOT);
        try {
            return ModelFormat.valueOf(formatStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Invalid model format '%s'. Valid values are: %s", formatStr, Arrays.toString(ModelFormat
                    .values())), e);
        }
    }

}
