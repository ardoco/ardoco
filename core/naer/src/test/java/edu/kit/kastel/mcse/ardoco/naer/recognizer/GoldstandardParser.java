/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.naer.recognizer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityReferenceType;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;

/**
 * This class is responsible for parsing goldstandard data into structured {@link NamedEntity} objects
 * and retrieving component-related information from a test project directory.
 */
public class GoldstandardParser {

    /**
     * Parses the goldstandards in the given path to {@link NamedEntity} objects.
     * <p>Attention: The created named entities will not have alternative names and each sentence occurrence will be DIRECT (because the current goldstandards
     * do not contain this information)! Also, the SAD reference will not be set.</p>
     *
     * @param goldstandardFilePath the path of the goldstandard file
     * @return the created {@link NamedEntity} objects
     * @throws IOException if the parsing fails
     */
    public static Set<NamedEntity> parse(Path goldstandardFilePath) throws IOException {
        String goldstandardAsString = Files.readString(goldstandardFilePath);

        //skip the first line because it's the CSV header
        int firstNewline = goldstandardAsString.indexOf('\n');
        goldstandardAsString = firstNewline >= 0 ? goldstandardAsString.substring(firstNewline + 1) : "";

        if (goldstandardAsString.isBlank()) {
            return new LinkedHashSet<>();
        }

        Map<String, NamedEntity> entitiesMap = new LinkedHashMap<>(); //map name -> NamedEntity

        for (String line : goldstandardAsString.split("\n")) {
            String[] parts = line.split(",");
            String name = parts[0].trim();
            int sentenceNumber = Integer.parseInt(parts[1].trim());

            if (entitiesMap.containsKey(name)) {
                entitiesMap.get(name).addOccurrence(sentenceNumber, NamedEntityReferenceType.DIRECT);
            } else {
                NamedEntity newComponent = new NamedEntity(name, NamedEntityType.COMPONENT);
                newComponent.addOccurrence(sentenceNumber, NamedEntityReferenceType.DIRECT);
                entitiesMap.put(name, newComponent);
            }
        }
        return new LinkedHashSet<>(entitiesMap.values());
    }

    /**
     * Retrieves possible component names from the goldstandard.
     *
     * @param projectDir the root directory of the test project, to locate the component names file
     * @return a map where the key is a {@link NamedEntityType#COMPONENT} and the value is a set of strings representing possible component names
     */
    public static Map<NamedEntityType, Set<String>> getPossibleComponents(Path projectDir) {
        Path componentNameFile = findComponentNameFile(projectDir);

        return parsePossibleComponents(componentNameFile);
    }

    private static Path findComponentNameFile(Path projectDir) {
        Path modelDir = assertDoesNotThrow(() -> Files.list(projectDir)
                .filter(Files::isDirectory)
                .filter(p -> p.getFileName().toString().startsWith("model_"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No model_ directory in " + projectDir)));

        Path umlDir = modelDir.resolve("uml");

        return assertDoesNotThrow(() -> Files.list(umlDir)
                .filter(p -> p.getFileName().toString().equals("modelElementID_to_ComponentName.csv"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No modelElementID_to_ComponentName.csv in " + umlDir)));
    }

    /**
     * Parses the specified file to extract possible component names and organizes them into a map.
     *
     * @param componentNameFile the path to the file containing component names
     * @return a map where the key is {@link NamedEntityType#COMPONENT}, and the value is a set of strings representing extracted component names
     */
    private static Map<NamedEntityType, Set<String>> parsePossibleComponents(Path componentNameFile) {
        String csvContent = assertDoesNotThrow(() -> Files.readString(componentNameFile));

        if (csvContent.isBlank()) {
            return new EnumMap<>(NamedEntityType.class);
        }

        List<String> lines = csvContent.lines().toList();
        Map<NamedEntityType, Set<String>> possibleComponents = new EnumMap<>(NamedEntityType.class);
        possibleComponents.put(NamedEntityType.COMPONENT, new TreeSet<>());

        for (int i = 1; i < lines.size(); i++) { // Skip header
            String[] parts = lines.get(i).split(",");
            if (parts.length >= 2) {
                String componentName = parts[1].trim();
                if (!componentName.isEmpty()) {
                    possibleComponents.get(NamedEntityType.COMPONENT).add(componentName);
                }
            }
        }

        return possibleComponents;
    }

}
