package edu.kit.kastel.mcse.ner_for_arch.recognizer;

import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntityReferenceType;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntityType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class GoldstandardParser {

    /**
     * Parses the goldstandards in the given path to {@link NamedEntity} objects.
     * <p>Attention: The created named entities will not have alternative names and each sentence occurrence will be DIRECT (because the current goldstandards do not contain this information)! Also, the SAD reference will not be set.</p>
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
            return new HashSet<>();
        }

        Map<String, NamedEntity> entitiesMap = new HashMap<>(); //map name -> NamedEntity

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
        return new HashSet<>(entitiesMap.values());
    }

    /**
     * Retrieves a comma-separated string of the goldstandard component names from the test project in the given path.
     *
     * @param projectDir the path to the project directory
     * @return a comma-separated string of component names, or an empty string if no component names are found
     */
    public static String getComponentNames(Path projectDir) {
        Path componentNameFile = findComponentNameFile(projectDir);

        return parseComponentNames(componentNameFile);
    }


    private static Path findComponentNameFile(Path projectDir) {
        Path modelDir = assertDoesNotThrow(() ->
                Files.list(projectDir)
                        .filter(Files::isDirectory)
                        .filter(p -> p.getFileName().toString().startsWith("model_"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No model_ directory in " + projectDir))
        );

        Path umlDir = modelDir.resolve("uml");

        return assertDoesNotThrow(() ->
                Files.list(umlDir)
                        .filter(p -> p.getFileName().toString().equals("modelElementID_to_ComponentName.csv"))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No modelElementID_to_ComponentName.csv in " + umlDir))
        );
    }


    private static String parseComponentNames(Path componentNameFile) {
        String csvContent = assertDoesNotThrow(() -> Files.readString(componentNameFile));

        if (csvContent.isBlank()) {
            return "";
        }

        List<String> lines = csvContent.lines().toList();
        List<String> componentNames = new ArrayList<>();

        for (int i = 1; i < lines.size(); i++) { // Skip header
            String[] parts = lines.get(i).split(",");
            if (parts.length >= 2) {
                String componentName = parts[1].trim();
                if (!componentName.isEmpty()) {
                    componentNames.add(componentName);
                }
            }
        }

        return String.join(", ", componentNames);
    }


}
