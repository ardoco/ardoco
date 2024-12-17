/* Licensed under MIT 2024. */
package edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.raw;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.tlr.models.connectors.generators.architecture.ArchitectureExtractor;

/**
 * An extractor for raw architecture models. Expected format:
 * <p>
 * Name1::ID<br>
 * Name2::ID
 * </p>
 */
public class RawArchitectureExtractor extends ArchitectureExtractor {
    private static final Logger logger = LoggerFactory.getLogger(RawArchitectureExtractor.class);

    public RawArchitectureExtractor(String modelPath) {
        super(modelPath);
    }

    @Override
    public ArchitectureModel extractModel() {
        try {
            List<String> lines = Files.readAllLines(Path.of(this.path));

            List<ArchitectureItem> components = new ArrayList<>();
            for (String line : lines) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split("::", 2);
                if (parts.length != 2) {
                    logger.warn("Line has no ID. Using random ID: {}", line);
                    parts = Arrays.copyOf(parts, 2);
                    parts[1] = parts[0] + "_" + UUID.randomUUID();
                }
                components.add(new ArchitectureComponent(parts[0].trim(), parts[1].trim(), new TreeSet<>(), new TreeSet<>(), new TreeSet<>(), "component"));
            }

            return new ArchitectureModel(components);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ModelType getModelType() {
        return ArchitectureModelType.RAW;
    }
}
