/* Licensed under MIT 2024-2025. */
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

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureComponentModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelWithComponentsAndInterfaces;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
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

    public RawArchitectureExtractor(String modelPath, Metamodel metamodelToExtract) {
        super(modelPath, metamodelToExtract);
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

            return switch (metamodelToExtract) {
                case Metamodel.ARCHITECTURE_WITH_COMPONENTS_AND_INTERFACES -> new ArchitectureModelWithComponentsAndInterfaces(components);
                case Metamodel.ARCHITECTURE_WITH_COMPONENTS -> new ArchitectureComponentModel(new ArchitectureModelWithComponentsAndInterfaces(components));
                default -> throw new IllegalArgumentException("Unsupported metamodel: " + metamodelToExtract);
            };

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
