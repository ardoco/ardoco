/* Licensed under MIT 2025. */
package edu.kit.kastel.mcse.ardoco.naer.serialization;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntity;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityReferenceType;
import edu.kit.kastel.mcse.ardoco.naer.model.NamedEntityType;
import edu.kit.kastel.mcse.ardoco.naer.model.SoftwareArchitectureDocumentation;
import edu.kit.kastel.mcse.ardoco.naer.recognizer.StructuredTextOutputPrompt;

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
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        Set<NamedEntity> entities = new LinkedHashSet<>();

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
                String line = occurrence.asText();
                addOccurrenceWithDeductedReferenceType(entity, sad.getLineNumber(line), sad);
            }

            entities.add(entity);
        }

        return entities;
    }

    /**
     * Parses a string representation of named entities into a set of {@link NamedEntity} instances.
     * The input string must follow the specific format created by {@link StructuredTextOutputPrompt}.
     *
     * @param str                               the string (structured text format of named entities)
     * @param softwareArchitectureDocumentation the software architecture documentation associated with the named entities
     * @return a set of parsed {@link NamedEntity} instances
     * @throws IOException if the input string is invalid or cannot be parsed
     */
    public static Set<NamedEntity> fromString(String str, SoftwareArchitectureDocumentation softwareArchitectureDocumentation) throws IOException {
        Map<String, NamedEntity> entityMap = new LinkedHashMap<>();
        Map<String, Set<Integer>> entityOccurencesMap = new LinkedHashMap<>(); //needed to determine reference types of the occurrences after information about alternative names is saved
        String[] lines = str.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].trim();
        }

        boolean parsingAlternativeNames = false;
        NamedEntityType currentEntityType = null;

        processLines(softwareArchitectureDocumentation, lines, parsingAlternativeNames, currentEntityType, entityMap, entityOccurencesMap);

        //add occurrences with correct reference types
        for (NamedEntity entity : entityMap.values()) {
            for (int lineNumber : entityOccurencesMap.get(entity.getName())) {
                addOccurrenceWithDeductedReferenceType(entity, lineNumber, softwareArchitectureDocumentation);
            }
        }

        return new LinkedHashSet<>(entityMap.values());
    }

    private static void processLines(SoftwareArchitectureDocumentation softwareArchitectureDocumentation, String[] lines, boolean parsingAlternativeNames,
            NamedEntityType currentEntityType, Map<String, NamedEntity> entityMap, Map<String, Set<Integer>> entityOccurencesMap) throws IOException {
        for (String line : lines) {
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
                throw new IOException("Entity type not specified before entries: '" + line + "'");
            }

            if (!parsingAlternativeNames) {
                parseEntityOccurrence(softwareArchitectureDocumentation, line, entityMap, currentEntityType, entityOccurencesMap);
            } else {
                parseAlternativeNames(line, entityMap);
            }
        }
    }

    private static void parseEntityOccurrence(SoftwareArchitectureDocumentation softwareArchitectureDocumentation, String line,
            Map<String, NamedEntity> entityMap, NamedEntityType currentEntityType, Map<String, Set<Integer>> entityOccurencesMap) throws IOException {
        // Parse entity occurrence: <name>, '<line>'
        Pattern pattern = Pattern.compile("^(.*?),\\s*'(.*)'$");
        Matcher matcher = pattern.matcher(line.trim());
        if (!matcher.matches()) {
            logger.error("Invalid entity occurrence format: '{}'", line);
            throw new IOException("Invalid entity occurrence format: '" + line + "'");
        }

        String name = matcher.group(1).trim();
        String textLine = matcher.group(2);
        int lineNumber = softwareArchitectureDocumentation.getLineNumber(textLine);

        NamedEntity entity = entityMap.get(name);
        if (entity == null) {
            entity = new NamedEntity(name, currentEntityType);
            entity.setSourceText(softwareArchitectureDocumentation);
            entityMap.put(name, entity);
            entityOccurencesMap.put(name, new LinkedHashSet<>());
        }
        entityOccurencesMap.get(name).add(lineNumber);
    }

    private static void parseAlternativeNames(String line, Map<String, NamedEntity> entityMap) throws IOException {
        // Parse alternative names: <componentName>: <alt1>, <alt2>, ...
        String[] parts = line.split(":");
        if (parts.length != 2) {
            logger.error("Invalid alternative names format: '{}'", line);
            throw new IOException("Invalid alternative names format: '" + line + "'");
        }

        String name = parts[0].trim();
        String alternativesStr = parts[1].trim();
        if (alternativesStr.equalsIgnoreCase("None")) {
            return;
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

    private static void addOccurrenceWithDeductedReferenceType(NamedEntity entity, int lineNumber,
            SoftwareArchitectureDocumentation softwareArchitectureDocumentation) {
        if (lineNumber == -1) {
            //to improve resilience, we skip invalid occurrences
            return;
        }

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
