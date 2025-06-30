package edu.kit.kastel.mcse.ner_for_arch.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntity;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntityReferenceType;
import edu.kit.kastel.mcse.ner_for_arch.model.NamedEntityType;
import edu.kit.kastel.mcse.ner_for_arch.model.SoftwareArchitectureDocumentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for parsing named entities from various input formats.
 */
public class NamedEntityParser {
    private static final Logger logger = LoggerFactory.getLogger(NamedEntityParser.class);

    private NamedEntityParser() {
        // utility class -> prevent instantiation
    }

    /**
     * Deserializes a JSON array representing named entities into a set of {@link NamedEntity} instances.
     *
     * @param json the JSON string to deserialize; must represent a JSON array of named entity instances
     * @param sad  the software architecture documentation associated with the entities
     * @return a set of deserialized {@link NamedEntity} instances
     * @throws IOException if deserialization fails (e.g. malformed JSON)
     */
    public static Set<NamedEntity> fromJson(String json, SoftwareArchitectureDocumentation sad) throws IOException {
        //System.out.println("Parsing: \n" + json);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        Set<NamedEntity> entities = new HashSet<>();

        for (JsonNode entityNode : rootNode) {
            String name = entityNode.get("name").asText();
            NamedEntityType type = NamedEntityType.valueOf(entityNode.get("type").asText());
            NamedEntity entity = new NamedEntity(name, type);
            entity.setSourceText(sad);

            // Handle alternative names
            JsonNode alternativeNamesNode = entityNode.get("alternativeNames");
            for (JsonNode altName : alternativeNamesNode) {
                entity.addAlternativeName(altName.asText());
            }

            // Handle occurrences
            JsonNode occurrencesNode = entityNode.get("occurrences");
            for (JsonNode occurrence : occurrencesNode) {
                // New format: occurrences is a list of integers
                int lineNumber = occurrence.asInt();
                addOccurrenceWithDeductedReferenceType(entity, lineNumber, sad);
            }

            entities.add(entity);
        }

        return entities;
    }

    /**
     * Parses a string representation of named entities into a set of {@link NamedEntity} instances.
     * The input string must follow the specific format created by prompts using the type {@link edu.kit.kastel.mcse.ner_for_arch.recognizer.PromptType#STRUCTURED_TEXT_OUTPUT_PROMPT}.
     *
     * @param str                               the string (structured text format of named entities)
     * @param softwareArchitectureDocumentation the software architecture documentation associated with the named entities
     * @return a set of parsed {@link NamedEntity} instances
     * @throws IOException if the input string is invalid or cannot be parsed
     */
    public static Set<NamedEntity> fromString(String str, SoftwareArchitectureDocumentation softwareArchitectureDocumentation) throws IOException {
        //System.out.println("Parsing: \n" + str);
        Map<String, NamedEntity> entityMap = new HashMap<>();
        Map<String, Set<Integer>> entityOccurencesMap = new HashMap<>(); //needed to determine reference types of the occurrences after information about alternative names is saved
        String[] lines = str.split("\\R");

        boolean parsingAlternativeNames = false;
        NamedEntityType currentEntityType = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            // Check for section header: "<currentEntityType> entities recognized:"
            if (line.endsWith("entities recognized:")) {
                parsingAlternativeNames = false;
                String typeString = line.substring(0, line.indexOf(" entities recognized:")).trim().toUpperCase();
                try {
                    currentEntityType = NamedEntityType.valueOf(typeString);
                } catch (IllegalArgumentException e) {
                    logger.error("Unknown entity type: '{}'", typeString);
                    throw new IOException("Unknown entity type: '" + typeString + "'");
                }
                continue;
            }

            if ((line.trim()).equalsIgnoreCase("Alternative names:")) {
                parsingAlternativeNames = true;
                continue;
            }

            if (currentEntityType == null) {
                logger.error("Entity type not specified before entries: '{}'", line);
                throw new IOException("Entity type not specified before entries: '" + line + "'");
            }

            if (!parsingAlternativeNames) {
                // Parse entity occurrence: <name>, <lineNumber>
                String[] parts = line.split(",");
                if (parts.length != 2) {
                    logger.error("Invalid entity occurrence format: '{}'", line);
                    throw new IOException("Invalid entity occurrence format: '" + line + "'");
                }

                String name = parts[0].trim();
                int lineNumber = Integer.parseInt(parts[1].trim());

                NamedEntity entity = entityMap.get(name);
                if (entity == null) {
                    entity = new NamedEntity(name, currentEntityType);
                    entity.setSourceText(softwareArchitectureDocumentation);
                    entityMap.put(name, entity);
                    entityOccurencesMap.put(name, new HashSet<>());
                }
                entityOccurencesMap.get(name).add(lineNumber);

            } else {
                // Parse alternative names: <componentName>: <alt1>, <alt2>, ...
                String[] parts = line.split(":");
                if (parts.length != 2) {
                    logger.error("Invalid alternative names format: '{}'", line);
                    throw new IOException("Invalid alternative names format: '" + line + "'");
                }

                String name = parts[0].trim();
                String alternativesStr = parts[1].trim();
                if (alternativesStr.equalsIgnoreCase("None")) {
                    continue;
                }

                NamedEntity entity = entityMap.get(name);
                if (entity != null) {
                    String[] alternatives = alternativesStr.split(",");
                    for (String alt : alternatives) {
                        entity.addAlternativeName(alt.trim());
                    }
                } else {
                    logger.error("Alternative names for unknown entity: '{}'", name);
                    throw new IOException("Alternative names for unknown entity: '" + name + "'");
                }


            }
        }

        //add occurrences with correct reference types
        for (NamedEntity entity : entityMap.values()) {
            for (int lineNumber : entityOccurencesMap.get(entity.getName())) {
                addOccurrenceWithDeductedReferenceType(entity, lineNumber, softwareArchitectureDocumentation);
            }
        }

        return new HashSet<>(entityMap.values());
    }

    private static void addOccurrenceWithDeductedReferenceType(NamedEntity entity, int lineNumber, SoftwareArchitectureDocumentation softwareArchitectureDocumentation) {
        boolean isDirect = softwareArchitectureDocumentation.getLine(lineNumber).toLowerCase().contains(entity.getName().toLowerCase());
        for (String alternativeName : entity.getAlternativeNames()) {
            if (softwareArchitectureDocumentation.getLine(lineNumber).toLowerCase().contains(alternativeName.toLowerCase())) {
                isDirect = true;
                break;
            }
        }
        NamedEntityReferenceType referenceType = isDirect ? NamedEntityReferenceType.DIRECT : NamedEntityReferenceType.INDIRECT;
        entity.addOccurrence(lineNumber, referenceType);
    }
}
