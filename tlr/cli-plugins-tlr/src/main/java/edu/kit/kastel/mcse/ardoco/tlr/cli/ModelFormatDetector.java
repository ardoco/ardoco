/* Licensed under MIT 2026. */
package edu.kit.kastel.mcse.ardoco.tlr.cli;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelFormat;

/**
 * Utility class for auto-detecting the model format from a file. The detection is based on file extension and, when
 * ambiguous, file content analysis.
 */
public final class ModelFormatDetector {

    private static final Logger logger = LoggerFactory.getLogger(ModelFormatDetector.class);

    private ModelFormatDetector() {
        // Utility class - prevent instantiation
    }

    /**
     * Detects the model format from the given file. Detection is based on file extension:
     * <ul>
     * <li>{@code .repository} - PCM (Palladio Component Model)</li>
     * <li>{@code .uml} - UML (Unified Modeling Language)</li>
     * <li>{@code .txt}, {@code .components} - COMPONENT_LISTING</li>
     * </ul>
     *
     * @param modelFile the model file to analyze
     * @return the detected ModelFormat, or PCM as default if detection fails
     */
    public static ModelFormat detect(File modelFile) {
        if (modelFile == null) {
            logger.warn("Model file is null, defaulting to PCM format");
            return ModelFormat.PCM;
        }

        String fileName = modelFile.getName().toLowerCase();

        // PCM format detection
        if (fileName.endsWith(".repository")) {
            logger.info("Detected PCM format from .repository extension");
            return ModelFormat.PCM;
        }

        // UML format detection
        if (fileName.endsWith(".uml")) {
            logger.info("Detected UML format from .uml extension");
            return ModelFormat.UML;
        }

        // Component listing format detection
        if (fileName.endsWith(".txt") || fileName.endsWith(".components")) {
            logger.info("Detected COMPONENT_LISTING format from file extension");
            return ModelFormat.COMPONENT_LISTING;
        }

        // Default to PCM if unable to detect
        logger.warn("Unable to detect model format from file '{}', defaulting to PCM", fileName);
        return ModelFormat.PCM;
    }
}
