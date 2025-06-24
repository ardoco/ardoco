package edu.kit.kastel.mcse.ner_for_arch.recognizer;

import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntityReferenceType;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GoldstandardParser {
    private static final Logger logger = LoggerFactory.getLogger(NamedEntityRecognizerTest.class);


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
}
